// Copyright 2000-2024 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.coderknock.codegen.tool.action

import com.coderknock.codegen.tool.gen.GenerationUtil
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.readText
import java.util.*
import javax.swing.Icon

/**
 * Action class to demonstrate how to interact with the IntelliJ Platform.
 * The only action this class performs is to provide the user with a popup dialog as feedback.
 * Typically, this class is instantiated by the IntelliJ Platform framework based on declarations
 * in the plugin.xml file.
 * But when added at runtime, this class is instantiated by an action group.
 */
class EnumIsXXXAction : AnAction {
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    /**
     * This default constructor is used by the IntelliJ Platform framework to instantiate this class based on plugin.xml
     * declarations. Only needed in [EnumIsXXXAction] class because a second constructor is overridden.
     */
    constructor() : super()

    /**
     * This constructor is used to support dynamically added menu actions.
     * It sets the text, description to be displayed for the menu item.
     * Otherwise, the default AnAction constructor is used by the IntelliJ Platform.
     *
     * @param text        The text to be displayed as a menu item.
     * @param description The description of the menu item.
     * @param icon        The icon to be used with the menu item.
     */
    // via DynamicActionGroup
    constructor(text: String?, description: String?, icon: Icon?) : super(text, description, icon)

    override fun actionPerformed(event: AnActionEvent) {
        // Using the event, create and show a dialog
        val currentProject = event.project
        val fileEditor = event.dataContext.getData(PlatformDataKeys.LAST_ACTIVE_FILE_EDITOR)
        val virtualFile = fileEditor!!.file
        // 从 file 中读取文件
        val title = event.presentation.description
        if (virtualFile != null && virtualFile.exists()) {
            // 准备要写入的内容，这里以字符串为例
            val result = GenerationUtil.enumIsXXX(virtualFile.readText())
            if (result.isSuccess()) {
                WriteAction.run<Throwable> {
                    // 写入内容，注意这会覆盖原有文件内容，如果需要追加内容，请使用appendText()方法
                    virtualFile.setBinaryContent(result.getData().toByteArray(Charsets.UTF_8))
                    // 可能需要刷新文件系统或通知编辑器内容已改变，根据具体需求调整
                    virtualFile.refresh(false, true)
                }

            } else {
                Messages.showMessageDialog(
                    currentProject,
                    result.message,
                    title,
                    Messages.getInformationIcon()
                )
            }

        } else {
            val message =
                StringBuilder("选择错误")
            // If an element is selected in the editor, add info about it.
            val selectedElement = event.getData(CommonDataKeys.NAVIGATABLE)
            if (selectedElement != null) {
                message.append("\nSelected Element: ").append(selectedElement)
            }

            Messages.showMessageDialog(
                currentProject,
                message.toString(),
                title,
                Messages.getInformationIcon()
            )
        }

    }

    override fun update(e: AnActionEvent) {
        // Set the availability based on whether a project is open
        val project = e.project
        e.presentation.isEnabledAndVisible = Objects.nonNull(project)
    }
}
