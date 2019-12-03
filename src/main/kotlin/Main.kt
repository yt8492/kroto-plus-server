import com.yt8492.krotosample.protobuf.MessageRequest
import com.yt8492.krotosample.protobuf.MessageServiceCoroutineGrpc
import com.yt8492.krotosample.protobuf.send
import io.grpc.ManagedChannelBuilder
import io.grpc.ServerBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking

@ExperimentalCoroutinesApi
fun main() {
    val port = 6565
    val server = ServerBuilder.forPort(port)
        .addService(MessageServiceImpl())
        .build()
        .start()

    val channel = ManagedChannelBuilder.forAddress("localhost", 6565)
        .usePlaintext()
        .build()
    val client = MessageServiceCoroutineGrpc
        .MessageServiceCoroutineStub.newStub(channel)
    println("--- Bidirectional Stream start ---")
    runBlocking {
        val (requestChannel, responseChannel) = client.bidirectionalStream()
        listOf("hoge", "fuga", "piyo").forEach {
            requestChannel.send {
                message = it
            }
        }
        requestChannel.close()
        responseChannel.consumeEach {
            println(it.message)
        }
    }
    println("--- Bidirectional Stream finish ---")
    println("--- Client Stream start ---")
    runBlocking {
        val (requestChannel, response) = client.clientStream()
        listOf("hgoe", "fuga", "piyo").forEach {
            requestChannel.send {
                message = it
            }
        }
        requestChannel.close()
        println(response.await().message)
    }
    println("--- Client Stream finish ---")
    println("--- Server Stream start ---")
    runBlocking {
        val request = MessageRequest {
            message = "hoge"
        }
        client.serverStream(request).consumeEach {
            println(it.message)
        }
    }
    println("--- Server Stream finish ---")
    server.shutdown()
}