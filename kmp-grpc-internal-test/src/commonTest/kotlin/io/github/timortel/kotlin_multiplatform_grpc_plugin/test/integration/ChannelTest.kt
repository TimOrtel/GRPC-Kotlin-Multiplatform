package io.github.timortel.kotlin_multiplatform_grpc_plugin.test.integration

import io.github.timortel.kmpgrpc.core.Channel
import io.github.timortel.kmpgrpc.core.StatusException
import io.github.timortel.kmpgrpc.test.TestServiceStub
import io.github.timortel.kmpgrpc.test.simpleMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

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
        closeJob.join()

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
}
