package com.coderknock.codegen.tool.dialog

import com.coderknock.codegen.tool.bundle.adaptedMessage
import com.coderknock.codegen.tool.comparator.EnumComparator
import com.coderknock.codegen.tool.git.GitService
import com.coderknock.codegen.tool.inspection.EnumConsistencyConfig
import com.coderknock.codegen.tool.model.EnumComparisonResult
import com.coderknock.codegen.tool.model.ParsedEnumInfo
import git4idea.repo.GitRepository
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.search.GlobalSearchScope
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

/**
 * 枚举比较对话框（全项目扫描）
 *
 * 功能：
 * 1. 列出项目所有可用分支供用户选择
 * 2. 后台扫描全项目所有枚举类
 * 3. 将每个枚举与目标分支内容进行比较
 * 4. 检查出重复值和不一致的枚举常量
 * 5. 在对话框中显示所有不一致结果
 * 6. 支持启用实时检查功能（编辑时自动检查）
 *
 * @param project IDEA 项目实例
 */
class EnumCompareDialog(project: Project) : DialogWrapper(project) {

    private val logger = Logger.getInstance(EnumCompareDialog::class.java)
    private val gitService = GitService()
    private val project: Project
    private var repository: GitRepository?
    private val branchComboBox: JComboBox<String>
    private val enableRealTimeCheckCheckBox: JCheckBox
    private val resultTextArea: JTextArea
    private val config = EnumConsistencyConfig.getInstance()

    init {
        this.project = project
        this.repository = gitService.getGitRepository(project)

        val branches = mutableListOf<String>()
        if (repository != null) {
            branches.addAll(gitService.getLocalBranches(repository!!))
            branches.addAll(gitService.getRemoteBranches(repository!!))
        }

        branchComboBox = JComboBox(branches.toTypedArray())
        enableRealTimeCheckCheckBox = JCheckBox(adaptedMessage("dialog.enable.real.time.check"))
        resultTextArea = JTextArea(15, 80)
        resultTextArea.lineWrap = true
        resultTextArea.wrapStyleWord = true
        resultTextArea.isEditable = false

        title = adaptedMessage("dialog.enum.compare.title")
        init()
        setSize(800, 600)

        // 如果已有配置，恢复状态
        if (config.isRealTimeCheckEnabled) {
            enableRealTimeCheckCheckBox.isSelected = true
            config.compareBranch?.let {
                branchComboBox.selectedItem = it
            }
        }
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout(10, 10))

        // 顶部选择区域
        val topPanel = JPanel(BorderLayout(5, 5))

        val branchPanel = JPanel(BorderLayout(5, 0))
        branchPanel.add(JLabel(adaptedMessage("dialog.select.branch.label")), BorderLayout.WEST)
        branchPanel.add(branchComboBox, BorderLayout.CENTER)
        topPanel.add(branchPanel, BorderLayout.NORTH)
        topPanel.add(enableRealTimeCheckCheckBox, BorderLayout.SOUTH)

        // 结果区域
        val scrollPane = JScrollPane(resultTextArea)
        scrollPane.preferredSize = Dimension(800, 400)

        panel.add(topPanel, BorderLayout.NORTH)
        panel.add(scrollPane, BorderLayout.CENTER)

        return panel
    }

    override fun doOKAction() {
        val selectedBranch = branchComboBox.selectedItem as? String
        if (selectedBranch.isNullOrBlank()) {
            Messages.showErrorDialog(
                project,
                adaptedMessage("dialog.select.branch.error"),
                title
            )
            return
        }

        val enableRealTime = enableRealTimeCheckCheckBox.isSelected
        config.setConfig(enableRealTime, selectedBranch)

        // 在后台执行扫描和比较
        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project,
            adaptedMessage("dialog.scanning.progress"),
            true
        ) {
            override fun run(indicator: ProgressIndicator) {
                val result = scanAndCompare(selectedBranch, indicator)
                SwingUtilities.invokeLater {
                    displayResult(result)
                }
            }
        })

        // 如果不需要实时检查，直接关闭对话框
        if (!enableRealTime) {
            super.doOKAction()
        }
    }

    private fun scanAndCompare(
        compareBranch: String,
        indicator: ProgressIndicator
    ): List<EnumComparisonResult> {
        val results = mutableListOf<EnumComparisonResult>()
        val repository = repository ?: return results

        // 扫描项目中所有枚举类
        val scope = GlobalSearchScope.projectScope(project)
        val psiFacade = JavaPsiFacade.getInstance(project)

        // 使用 JavaPsiFacade 查找所有枚举类
        // 这里通过搜索所有 Java 类然后过滤出枚举
        indicator.isIndeterminate = false

        // 获取所有枚举类
        val enumClasses = mutableListOf<com.intellij.psi.PsiClass>()
        psiFacade.processAllClasses(scope) { psiClass ->
            if (psiClass.isEnum) {
                enumClasses.add(psiClass)
            }
            true
        }

        indicator.fraction = 0.0
        val total = enumClasses.size
        var processed = 0

        val currentBranch = gitService.getCurrentBranch(repository) ?: "local"

        for (psiClass in enumClasses) {
            indicator.checkCanceled()
            processed++
            indicator.fraction = processed.toDouble() / total.toDouble()
            indicator.text = "Processing ${psiClass.name}"

            val virtualFile = psiClass.containingFile?.virtualFile ?: continue
            val relativePath = gitService.getRelativePath(project, virtualFile)
            val remoteContent = gitService.getFileContentFromBranch(
                repository, compareBranch, relativePath
            )

            val localContent = psiClass.containingFile?.text ?: continue

            try {
                val localInfo = EnumComparator.parseEnum(localContent)
                // 先检查本地重复值
                val localCheck = EnumComparator.compare(localInfo, null, currentBranch, compareBranch)
                if (localCheck.hasConflicts()) {
                    localCheck.setLocalBranch(currentBranch)
                    localCheck.setRemoteBranch(compareBranch)
                    results.add(localCheck)
                }

                // 如果远端有这个文件，进行比较
                if (remoteContent != null) {
                    val remoteInfo = EnumComparator.parseEnum(remoteContent)
                    val comparison = EnumComparator.compare(
                        localInfo, remoteInfo, currentBranch, compareBranch
                    )
                    if (comparison.hasConflicts()) {
                        results.add(comparison)
                    }
                }
            } catch (e: Exception) {
                logger.warn("Failed to process ${psiClass.qualifiedName}: ${e.message}")
            }
        }

        return results
    }

    private fun displayResult(results: List<EnumComparisonResult>) {
        if (results.isEmpty()) {
            resultTextArea.text = adaptedMessage("dialog.no.conflicts.found")
            return
        }

        val sb = StringBuilder()
        var totalConflicts = 0
        var totalDuplicates = 0

        for (result in results) {
            totalConflicts += result.conflicts.size
            totalDuplicates += result.duplicateValues.size
        }

        sb.appendLine(adaptedMessage("dialog.summary.line", results.size, totalConflicts, totalDuplicates))
        sb.appendLine()

        for ((index, result) in results.withIndex()) {
            sb.appendLine("${index + 1}. ${result.enumName}")
            sb.appendLine("   ${adaptedMessage("dialog.branch.comparison", result.localBranch ?: "local", result.remoteBranch)}")

            for (duplicate in result.duplicateValues) {
                sb.appendLine("   ⚠️ ${duplicate.message}")
            }

            for (conflict in result.conflicts) {
                sb.appendLine("   ⚠️ ${conflict.message}")
            }
            sb.appendLine()
        }

        resultTextArea.text = sb.toString()
    }

    companion object {
        private const val PREFERRED_WIDTH = 800
        private const val PREFERRED_HEIGHT = 600
    }
}
