package com.coderknock.codegen.tool.action

import com.coderknock.codegen.tool.bundle.adaptedMessage
import com.coderknock.codegen.tool.gen.GenerationUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages

class EnumLookupAction : DumbAwareAction(
    adaptedMessage("action.EnumLookupAction.text"),
    adaptedMessage("action.EnumLookupAction.description"),
    null
) {
    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun actionPerformed(event: AnActionEvent) {
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return
        val result = GenerationUtil.enumLookup(editor.document.text)
        if (result.isSuccess) {
            WriteAction.run<Throwable> { editor.document.setText(result.data) }
        } else {
            Messages.showMessageDialog(event.project, result.message, event.presentation.text, Messages.getInformationIcon())
        }
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabledAndVisible = event.project != null && event.getData(CommonDataKeys.EDITOR) != null
    }
}
