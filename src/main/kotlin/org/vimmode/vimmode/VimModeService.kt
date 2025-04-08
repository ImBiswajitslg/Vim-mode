package org.vimmode.vimmode

import org.vimmode.vimmode.rpc.NeovimRpcClient
import java.io.File

class VimModeService {
    private var client: NeovimRpcClient? = null

    fun activateVimMode() {
        val socketPath = "/tmp/nvimsocket"
        val socketFile = File(socketPath)

        if (!socketFile.exists()) {
            Runtime.getRuntime().exec("nvim --embed --listen $socketPath")

            // Wait for socket to appear (max 5 seconds)
            val maxAttempts = 50
            var attempt = 0
            while (!socketFile.exists() && attempt < maxAttempts) {
                Thread.sleep(100)
                attempt++
            }

            if (!socketFile.exists()) {
                println("Socket failed to appear. Neovim may not have started.")
                return
            }
        }

        client = NeovimRpcClient(socketPath)
        client?.connect()
        client?.send("nvim_command", listOf("set modifiable"))
        client?.send("nvim_command", listOf("enew"))
        client?.send("nvim_command", listOf("echo 'Vim Mode activated!'"))

    }

    fun sendKey(key: String) {
        println("Sending key: $key")

        if (key.startsWith(":")) {
            // Ex command (e.g. :enew, :w)
            client?.send("nvim_command", listOf(key.drop(1))) // Drop the colon before sending
        } else {
            // Raw key input (i, j, k, etc.)
            client?.send("nvim_input", listOf(key))
        }
    }

    fun getClient(): NeovimRpcClient? {
        return client
    }

    companion object {
        private val instance = VimModeService()
        fun getInstance(): VimModeService = instance
    }
}
