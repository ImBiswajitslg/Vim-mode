package org.vimmode.vimmode.rpc

import org.newsclub.net.unix.AFUNIXSocket
import org.newsclub.net.unix.AFUNIXSocketAddress
import org.msgpack.core.MessageBufferPacker
import org.msgpack.core.MessagePack
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.Executors
import org.msgpack.value.Value

class NeovimRpcClient(private val socketPath: String = "/tmp/nvimsocket") {

    private lateinit var socket: AFUNIXSocket
    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream

    private val readerExecutor = Executors.newSingleThreadExecutor()
    private val pendingCallbacks = mutableMapOf<Int, (Value?) -> Unit>()
    private var currentRequestId = 1

    fun connect() {
        val socketFile = File(socketPath)
        socket = AFUNIXSocket.newInstance()
        socket.connect(AFUNIXSocketAddress(socketFile))
        inputStream = socket.getInputStream()
        outputStream = socket.getOutputStream()
        println("Connected to Neovim via $socketPath")

        startReader()
    }

    private fun startReader() {
        readerExecutor.submit {
            try {
                val unpacker = MessagePack.newDefaultUnpacker(inputStream)
                while (true) {
                    val message = unpacker.unpackValue()
                    val array = message.asArrayValue().list()

                    val msgType = array[0].asIntegerValue().asInt()

                    if (msgType == 1) { // Response
                        val requestId = array[1].asIntegerValue().asInt()
                        val error = array[2]
                        val result = array[3]

                        val callback = pendingCallbacks.remove(requestId)
                        callback?.invoke(if (error.isNilValue) result else null)
                    } else {
                        println("[Neovim Notification] $message")
                    }
                }
            } catch (e: Exception) {
                println("Error reading from Neovim: ${e.message}")
            }
        }
    }

    fun send(method: String, args: List<Any>, callback: ((Value?) -> Unit)? = null) {
        val packer: MessageBufferPacker = MessagePack.newDefaultBufferPacker()
        val requestId = currentRequestId++

        packer.packArrayHeader(4)
        packer.packInt(0) // Request type = 0
        packer.packInt(requestId)
        packer.packString(method)
        packer.packArrayHeader(args.size)
        for (arg in args) {
            when (arg) {
                is String -> packer.packString(arg)
                is Boolean -> packer.packBoolean(arg)
                is Int -> packer.packInt(arg)
                else -> throw IllegalArgumentException("Unsupported arg type: ${arg::class}")
            }
        }

        packer.flush()
        val bytes = packer.toByteArray()
        outputStream.write(bytes)
        outputStream.flush()

        if (callback != null) {
            pendingCallbacks[requestId] = callback
        }
    }

    fun close() {
        socket.close()
        readerExecutor.shutdownNow()
    }
}
