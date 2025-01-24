// Copyright 2000-2023 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.coderknock.codegen.tool.group

import com.coderknock.codegen.tool.bundle.adaptedMessage
import com.coderknock.codegen.tool.bundle.message
import com.coderknock.codegen.tool.icons.SdkIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.util.NlsActions
import java.util.*
import java.util.function.Supplier
import javax.swing.Icon

/**
 * Creates an action group to contain menu actions. See plugin.xml declarations.
 * 创建操作组以包含菜单操作。请参见 plugin. xml 声明。
 */
class CodeExtendGroup(
    text: Supplier<@NlsActions.ActionText String> = Supplier { adaptedMessage("group.CodeExtendGroup.text") },
    dynamicDescription: Supplier<@NlsActions.ActionText String> = Supplier { adaptedMessage("group.CodeExtendGroup.description") },
    icon: Icon? = null
) : DefaultActionGroup(text, dynamicDescription, icon) {
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun update(event: AnActionEvent) {
        // Enable/disable depending on whether user is editing
        val editor = event.getData(CommonDataKeys.EDITOR)
        event.presentation.isEnabled = Objects.nonNull(editor)
        // Take this opportunity to set an icon for the group.
        event.presentation.setIcon(SdkIcons.CODE)
    }
}
