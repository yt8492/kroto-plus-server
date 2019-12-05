import com.yt8492.krotosample.protobuf.MessageRequest
import com.yt8492.krotosample.protobuf.MessageResponse
import com.yt8492.krotosample.protobuf.MessageServiceCoroutineGrpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.toList
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class MessageServiceImpl : MessageServiceCoroutineGrpc.MessageServiceImplBase() {
    override val initialContext: CoroutineContext
        get() = Dispatchers.Default + SupervisorJob()

    override suspend fun clientStream(
        requestChannel: ReceiveChannel<MessageRequest>
    ): MessageResponse {
        val requestList = requestChannel.toList()
        return MessageResponse {
            message = requestList.joinToString("\n") {
                it.message.toUpperCase()
            }
        }
    }

    override suspend fun serverStream(
        request: MessageRequest,
        responseChannel: SendChannel<MessageResponse>
    ) {
        val response = MessageResponse {
            message = request.message.toUpperCase()
        }
        repeat(2) {
            responseChannel.send(response)
        }
        responseChannel.close()
    }

    override suspend fun bidirectionalStream(
        requestChannel: ReceiveChannel<MessageRequest>,
        responseChannel: SendChannel<MessageResponse>
    ) {
        requestChannel.consumeEach { request ->
            val response = MessageResponse {
                message = request.message.toUpperCase()
            }
            responseChannel.send(response)
        }
    }
}