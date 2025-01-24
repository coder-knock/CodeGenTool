package com.coderknock.codegen.tool.action

import com.coderknock.codegen.tool.bundle.adaptedMessage
import com.coderknock.codegen.tool.comparator.EnumComparator
import com.coderknock.codegen.tool.dialog.EnumCompareResultDialog
import com.coderknock.codegen.tool.git.GitService
import com.coderknock.codegen.tool.model.EnumComparisonResult
import com.coderknock.codegen.tool.model.ParsedEnumInfo
import git4idea.repo.GitRepository
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.Icon

/**
 * 当前枚举与指定分支比较动作
 *
 * 功能：只针对当前编辑器中打开的枚举类进行比较
 * 适用场景：开发者正在修改一个枚举，想快速对比与目标分支（如 main/master）的差异
 *
 * 比较流程：
 * 1. 获取当前打开的文件，检查是否为 Java 枚举
 * 2. 从 IDEA 获取 Git 仓库信息
 * 3. 让用户选择要比较的分支
 * 4. 从目标分支获取枚举内容进行解析比较
 * 5. 显示比较结果
 */
class CurrentEnumCompareAction(icon: Icon? = null) : DumbAwareAction({
    adaptedMessage("action.CurrentEnumCompareAction.text")
}, { adaptedMessage("action.CurrentEnumCompareAction.description") }, icon) {

    private val logger = Logger.getInstance(CurrentEnumCompareAction::class.java)
    private val gitService = GitService()

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return
        val file = event.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        // 检查是否是 Java 文件
        if (!file.name.endsWith(".java")) {
            Messages.showWarningDialog(
                project,
                adaptedMessage("error.not.java.file"),
                title
            )
            return
        }

        // 检查是否是 Git 项目
        val repository = gitService.getGitRepository(project)
        if (repository == null) {
            Messages.showWarningDialog(
                project,
                adaptedMessage("error.not.git.project"),
                title
            )
            return
        }

        // 让用户选择分支
        val branches = mutableListOf<String>()
        branches.addAll(gitService.getLocalBranches(repository))
        branches.addAll(gitService.getRemoteBranches(repository))

        if (branches.isEmpty()) {
            Messages.showWarningDialog(
                project,
                adaptedMessage("error.no.branches.found"),
                title
            )
            return
        }

        val selectedBranch = Messages.showInputDialogWithCheckBox(
            adaptedMessage("dialog.select.branch.to.compare"),
            title,
            adaptedMessage("dialog.enable.real.time.check"),
            false,
            null,
            project
        )

        if (selectedBranch == null || selectedBranch.first.isNullOrBlank()) {
            return
        }

        val branchName = selectedBranch.first
        val enableRealTime = selectedBranch.second

        // 执行比较
        val currentContent = String(file.contentsToByteArray(), Charsets.UTF_8)
        val relativePath = gitService.getRelativePath(project, file)
        val remoteContent = gitService.getFileContentFromBranch(repository, branchName, relativePath)

        try {
            val localInfo = EnumComparator.parseEnum(currentContent)
            val currentBranch = gitService.getCurrentBranch(repository) ?: "local"

            val result: EnumComparisonResult

            if (remoteContent != null) {
                val remoteInfo = EnumComparator.parseEnum(remoteContent)
                result = EnumComparator.compare(localInfo, remoteInfo, currentBranch, branchName)
            } else {
                result = EnumComparator.compare(localInfo, null, currentBranch, branchName)
                if (remoteContent == null) {
                    Messages.showWarningDialog(
                        project,
                        adaptedMessage("warning.enum.not.found.in.branch", branchName),
                        title
                    )
                }
            }

            // 显示结果对话框
            val resultDialog = EnumCompareResultDialog(project, result, branchName)
            resultDialog.show()

        } catch (e: Exception) {
            logger.error("Failed to compare enum", e)
            Messages.showErrorDialog(
                project,
                adaptedMessage("error.compare.failed", e.message),
                title
            )
        }
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = project != null && file != null && file.name.endsWith(".java")
    }
}
