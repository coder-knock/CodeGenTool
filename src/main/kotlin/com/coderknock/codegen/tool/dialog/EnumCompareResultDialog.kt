package com.coderknock.codegen.tool.dialog

import com.coderknock.codegen.tool.bundle.adaptedMessage
import com.coderknock.codegen.tool.model.EnumComparisonResult
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

/**
 * 枚举比较结果显示对话框
 */
class EnumCompareResultDialog(
    project: Project,
    private val result: EnumComparisonResult,
    private val branchName: String
) : DialogWrapper(project) {

    private val resultTextArea: JTextArea

    init {
        title = adaptedMessage("dialog.compare.result.title", result.enumName, branchName)
        resultTextArea = JTextArea(20, 70)
        resultTextArea.lineWrap = true
        resultTextArea.wrapStyleWord = true
        resultTextArea.isEditable = false
        init()
        setSize(800, 500)
        displayResult()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        val scrollPane = JScrollPane(resultTextArea)
        scrollPane.preferredSize = Dimension(700, 400)
        panel.add(scrollPane, BorderLayout.CENTER)
        return panel
    }

    private fun displayResult() {
        if (!result.hasConflicts) {
            resultTextArea.text = adaptedMessage("dialog.no.conflicts.found")
            return
        }

        val sb = StringBuilder()
        sb.appendLine(adaptedMessage("dialog.comparison.result.for", result.enumName))
        sb.appendLine(adaptedMessage("dialog.branch.comparison", result.localBranch ?: "local", result.remoteBranch))
        sb.appendLine()

        // 显示本地重复值
        if (result.duplicateValues.isNotEmpty()) {
            sb.appendLine(adaptedMessage("dialog.duplicate.values.found"))
            for (duplicate in result.duplicateValues) {
                sb.appendLine("  ⚠️ ${duplicate.message}")
            }
            sb.appendLine()
        }

        // 显示冲突
        if (result.conflicts.isNotEmpty()) {
            sb.appendLine(adaptedMessage("dialog.conflicts.found"))
            for (conflict in result.conflicts) {
                sb.appendLine("  ⚠️ ${conflict.message}")
            }
        }

        resultTextArea.text = sb.toString()
    }
}
