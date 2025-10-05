package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kmpgrpc.core.Channel
import io.github.timortel.kmpgrpc.core.config.KeepAliveConfig
import io.github.timortel.kmpgrpc.test.SimpleMessage
import io.github.timortel.kmpgrpc.test.TestServiceStub
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class KeepAliveTest : ServerTestImpl {

    @Test
    fun testKeepAliveConnection() = runTest {
        val channel = Channel.Builder.forAddress(address, port)
            .usePlaintext()
            .withKeepAliveConfig(
                KeepAliveConfig.Enabled(
                    time = 15.seconds,
                    timeout = 20.seconds,
                    withoutCalls = true
                )
            )
            .build()

        val stub = TestServiceStub(channel)

        // Test that the channel works with keepAlive configuration
        val responses = stub.bidiStreamingRpc(
            flow {
                emit(SimpleMessage())

                withContext(Dispatchers.Default) {
                    delay(60.seconds)
                }

                emit(SimpleMessage())
            }
        ).toList()

        assertEquals(2, responses.size, "Did not receive expected number of responses")

        channel.shutdown()
    }
}
