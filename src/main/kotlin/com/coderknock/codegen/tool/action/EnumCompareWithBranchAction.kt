package com.coderknock.codegen.tool.action

import com.coderknock.codegen.tool.bundle.adaptedMessage
import com.coderknock.codegen.tool.dialog.EnumCompareDialog
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAwareAction
import java.util.*
import javax.swing.Icon

/**
 * 枚举与指定分支比较动作（全项目扫描）
 *
 * 功能：扫描整个项目中所有枚举类，逐个与指定分支比较
 * 适用场景：批量检查项目中所有枚举与目标分支是否一致，找出不一致的枚举
 *
 * 比较流程：
 * 1. 打开对话框让用户选择分支
 * 2. 在对话框中进行全项目扫描和比较
 * 3. 显示所有不一致的枚举结果
 */
class EnumCompareWithBranchAction(icon: Icon? = null) : DumbAwareAction({
    adaptedMessage("action.EnumCompareWithBranchAction.text")
}, { adaptedMessage("action.EnumCompareWithBranchAction.description") }, icon) {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val dialog = EnumCompareDialog(project)
        dialog.show()
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        e.presentation.isEnabledAndVisible = Objects.nonNull(project)
    }
}
