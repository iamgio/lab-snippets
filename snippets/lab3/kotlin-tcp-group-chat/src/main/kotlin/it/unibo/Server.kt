package it.unibo

import io.ktor.network.sockets.ServerSocket
import io.ktor.network.sockets.TcpSocketBuilder
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.readUTF8Line
import it.unibo.protocol.ProtocolMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 *
 */
class Server(
    override val scope: CoroutineScope,
    override val host: String,
    override val port: Int,
    private val socketBuilder: TcpSocketBuilder,
    private val onReceive: ServerCallback,
) : Addressable, Process {
    private lateinit var serverSocket: ServerSocket
    private var sendChannels = mutableSetOf<ByteWriteChannel>()

    override suspend fun start() {
        serverSocket = socketBuilder.bind(host, port)
        println("Server is listening at ${serverSocket.localAddress}")
    }

    override suspend fun update() {
        val socket = serverSocket.accept()
        println("Accepted $socket")

        scope.launch {
            val receiveChannel = socket.openReadChannel()
            val sendChannel = socket.openWriteChannel(autoFlush = true)
            sendChannels.add(sendChannel)

            try {
                while (true) {
                    val message =
                        receiveChannel.readUTF8Line()
                            ?.let(ProtocolMessage::decode)
                            ?: continue
                    onReceive(ReceivedMessage(message, sendChannels))
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                stop()
            }
        }
    }

    override suspend fun stop() {
        scope.launch {
            serverSocket.close()
        }
    }
}
