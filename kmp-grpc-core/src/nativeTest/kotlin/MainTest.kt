import io.github.timortel.kmpgrpc.nativerust.*
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.staticCFunction
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.test.Test

class MainTest {
    @Test
    fun myTest() = runTest {
        suspendCoroutine { continuation: Continuation<Unit> ->
            val requestChannel = request_channel_create()

            rpc_implementation(
                host = "http://localhost:17888",
                path = "/io.github.timortel.kmpgrpc.test.TestService/emptyRpc",
                request_channel = requestChannel,
                user_data = StableRef.create(continuation).asCPointer(),
                serialize_request = staticCFunction { message ->
                    println("SERIALIZE")

                    c_byte_array_create(null, 0u)
                },
                deserialize_response = staticCFunction { ptr, length ->
                    StableRef.create("some message").asCPointer()
                },
                on_message_received = staticCFunction { data, message ->
                    val msg = message!!.asStableRef<String>()
                    println("RECEIVED $msg")
                },
                on_done = staticCFunction { data, code, message ->
                    println("Done!")
                    data!!.asStableRef<Continuation<Unit>>().get().resume(Unit)
                }
            )

            println("STARTING SEND")
            request_channel_send(requestChannel, null)
            println("ENDING SEND")

        }

        throw IllegalStateException("5")
    }
}
