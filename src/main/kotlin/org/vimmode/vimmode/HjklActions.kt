package org.vimmode.vimmode

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

class HjklActions : AnAction("Send Vim Key") {
    override fun actionPerformed(e: AnActionEvent) {
        val key = Messages.showInputDialog("Enter Vim Key (e.g. h, j, k, l):", "Send Key", null)
        if (key != null && key.isNotBlank()) {
            VimModeService.getInstance().sendKey(key)
        }
    }
}