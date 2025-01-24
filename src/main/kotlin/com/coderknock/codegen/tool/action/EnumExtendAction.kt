// Copyright 2000-2024 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.coderknock.codegen.tool.action

import com.coderknock.codegen.tool.bundle.adaptedMessage
import com.coderknock.codegen.tool.gen.GenerationUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages
import java.util.*
import javax.swing.Icon

/**
 * Action class to demonstrate how to interact with the IntelliJ Platform.
 * The only action this class performs is to provide the user with a popup dialog as feedback.
 * Typically, this class is instantiated by the IntelliJ Platform framework based on declarations
 * in the plugin.xml file.
 * But when added at runtime, this class is instantiated by an action group.
 */
class EnumExtendAction(icon: Icon? = null) : DumbAwareAction({
    adaptedMessage("action.EnumExtendAction.text")
}, { adaptedMessage("action.EnumExtendAction.description") }, icon) {
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    override fun actionPerformed(event: AnActionEvent) {
        // Using the event, create and show a dialog
        val currentProject = event.project
        val fileEditor = event.getRequiredData(CommonDataKeys.EDITOR)
        val document = fileEditor.document
        // 从 file 中读取文件
        val title = event.presentation.description
        // 准备要写入的内容，这里以字符串为例
        val result = GenerationUtil.enumIsXXX(document.text);
        if (result.isSuccess()) {
            WriteAction.run<Throwable> {
                document.setText(result.getData())
            }
        } else {
            Messages.showMessageDialog(
                currentProject, result.message, title, Messages.getInformationIcon()
            )
        }
    }

    override fun update(e: AnActionEvent) {
        // Set the availability based on whether a project is open
        val project = e.project
        e.presentation.isEnabledAndVisible = Objects.nonNull(project)
    }
}
