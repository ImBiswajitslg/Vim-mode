package org.vimmode.vimmode

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import org.msgpack.value.Value

class GetModeAction : AnAction("Get Neovim Mode") {
    override fun actionPerformed(e: AnActionEvent) {
        try {
            val client = VimModeService.getInstance().getClient()
            if (client == null) {
                Messages.showErrorDialog("Neovim client not connected.", "Vim Mode")
                return
            }

            println("Calling nvim_get_mode...")

            client.send("nvim_get_mode", emptyList<Value>()) { result ->
                println("Got result from Neovim: $result")
                val map = result?.map()
                val mode = map?.getOrDefault(MessagePack.newDefaultUnpacker("mode".toByteArray()).unpackValue(), null)
                Messages.showInfoMessage("Current Neovim mode: $mode", "Vim Mode")
            }

        } catch (ex: Exception) {
            Messages.showErrorDialog("Failed to get Neovim mode: ${ex.message}", "Vim Mode")
        }
    }
}
