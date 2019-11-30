import com.yt8492.grpcchat.protobuf.ChatServiceCoroutineGrpc
import com.yt8492.grpcchat.protobuf.Empty
import com.yt8492.grpcchat.protobuf.MessageRequest
import com.yt8492.grpcchat.protobuf.MessageResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class ChatServiceImpl : ChatServiceCoroutineGrpc.ChatServiceImplBase() {
    override val initialContext: CoroutineContext
        get() = Dispatchers.Default + SupervisorJob()

    private val connections = ConcurrentHashMap.newKeySet<SendChannel<MessageResponse>>()

    override suspend fun execStream(
        requestChannel: ReceiveChannel<MessageRequest>,
        responseChannel: SendChannel<MessageResponse>
    ) {
        connections.add(responseChannel)
        requestChannel.consumeEach { request ->
            val messageRes = MessageResponse {
                message = request.message
            }
            connections.forEach {
                it.send(messageRes)
            }
        }
    }

    override suspend fun healthCheck(request: Empty): Empty {
        return Empty {
        }
    }
}