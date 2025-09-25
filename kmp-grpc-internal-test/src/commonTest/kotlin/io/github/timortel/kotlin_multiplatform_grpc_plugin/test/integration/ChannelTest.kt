package io.github.timortel.kotlin_multiplatform_grpc_plugin.test.integration

import io.github.timortel.kmpgrpc.core.Channel
import io.github.timortel.kmpgrpc.core.StatusException
import io.github.timortel.kmpgrpc.test.TestServiceStub
import io.github.timortel.kmpgrpc.test.simpleMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

abstract class ChannelTest : ServerTest {

    @Test
    fun testChannelShutdownWaitsUntilAllRpcsAreFinished() = runTest {
        val channel = Channel.Builder.forAddress(address, port)
            .usePlaintext()
            .build()

        val stub = TestServiceStub(channel)

        assertFalse("By default, a channel is not terminated") { channel.isTerminated }

        val rpcJob = launch {
            stub.unaryDelayed(simpleMessage { })
        }

        val closeJob = launch {
            channel.shutdown()
        }

        // Channel shutdown cannot have finished yet
        assertFalse("Channel cannot be terminated already") { channel.isTerminated }

        rpcJob.cancelAndJoin()

        // Now it should be terminated, as all rpcs are done
        assertTrue("Expected the close job to have finished") { closeJob.isCompleted }
        assertTrue("Expected the channel to be terminated") { channel.isTerminated }
    }

    @Test
    fun testChannelShutdownNowCancelsAllRpcs() = runTest {
        val channel = Channel.Builder.forAddress(address, port)
            .usePlaintext()
            .build()

        val stub = TestServiceStub(channel)

        assertFalse("By default, a channel is not terminated") { channel.isTerminated }

        val rpcJob = launch {
            assertFailsWith<StatusException> { stub.unaryDelayed(simpleMessage { }) }
        }

        // The shutdownNow must not wait for the RPC to finish
        withContext(Dispatchers.Default) {
            withTimeout(400.milliseconds) {
                channel.shutdownNow()

                rpcJob.join()
            }
        }

        // Now it should be terminated, as all rpcs are done
        assertTrue("Expected the rpc job to have finished") { rpcJob.isCompleted }
        assertTrue("Expected the channel to be terminated") { channel.isTerminated }
    }

    @Test
    fun testKeepAliveConfiguration() = runTest {
        val channel = Channel.Builder.forAddress(address, port)
            .usePlaintext()
            .keepAliveTime(30.seconds)
            .keepAliveTimeout(5.seconds)
            .keepAliveWithoutCalls(true)
            .build()

        val stub = TestServiceStub(channel)

        // Test that the channel works with keepAlive configuration
        val response = stub.simpleRpc(simpleMessage { })
        assertTrue("Channel should work with keepAlive configuration") { response != null }

        channel.shutdown()
        assertTrue("Channel should be terminated after shutdown") { channel.isTerminated }
    }
}