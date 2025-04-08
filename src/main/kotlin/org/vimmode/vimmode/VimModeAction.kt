package org.vimmode.vimmode

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

import org.vimmode.vimmode.rpc.NeovimRpcClient;

import java.io.File;

class VimModeAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        try {
            val socketPath = "/tmp/nvimsocket"
            if (!File(socketPath).exists()) {
                Runtime.getRuntime().exec("nvim --embed --listen $socketPath")
                Thread.sleep(1000) // Give it time to start
            }

            val client = NeovimRpcClient(socketPath)
            client.connect()
            client.send("nvim_command", listOf("echo 'Vim Mode activated!'"))

            Messages.showInfoMessage("Neovim connected via socket successfully!", "Vim Mode")
        } catch (ex: Exception) {
            Messages.showErrorDialog("Failed to connect to Neovim: ${ex.message}", "Vim Mode")
        }
    }
}