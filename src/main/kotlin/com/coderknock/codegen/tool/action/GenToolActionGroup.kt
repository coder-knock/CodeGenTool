// Copyright 2000-2023 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.coderknock.codegen.tool.action

import com.coderknock.codegen.tool.icons.SdkIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DefaultActionGroup
import java.util.*

/**
 * Creates an action group to contain menu actions. See plugin.xml declarations.
 * 创建操作组以包含菜单操作。请参见 plugin. xml 声明。
 */
class GenToolActionGroup : DefaultActionGroup() {
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
