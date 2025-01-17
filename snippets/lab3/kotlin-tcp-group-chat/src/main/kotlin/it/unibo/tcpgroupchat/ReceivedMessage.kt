package it.unibo.tcpgroupchat

import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeStringUtf8
import it.unibo.tcpgroupchat.protocol.ProtocolMessage

/**
 * A message received by a [Process].
 * @property message the received message
 * @property sendChannels the writable channels to send replies to
 */
data class ReceivedMessage(val message: ProtocolMessage, val sendChannels: Set<ByteWriteChannel>) {
    val uuid get() = message.uuid
    val type get() = message.type
    val text get() = message.text

    constructor(message: ProtocolMessage, sendChannel: ByteWriteChannel) : this(message, setOf(sendChannel))

    /**
     * Sends a message to the sender.
     * @param message the message to send
     */
    suspend fun replyBroadcast(message: ProtocolMessage) {
        sendChannels.forEach { sendChannel ->
            if (!sendChannel.isClosedForWrite) {
                sendChannel.writeStringUtf8("${message.encode()}\n")
            }
        }
    }
}
