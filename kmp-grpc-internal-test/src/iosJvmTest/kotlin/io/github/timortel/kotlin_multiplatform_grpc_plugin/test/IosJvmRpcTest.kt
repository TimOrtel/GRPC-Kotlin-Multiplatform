package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kmpgrpc.test.CancellationServiceStub
import io.github.timortel.kmpgrpc.test.cancellationMessage
import io.github.timortel.kmpgrpc.test.simpleMessage
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class IosJvmRpcTest : RpcTest() {

    @Test
    fun testClientStream() = runTest {
        val content = "abc"

        val message = simpleMessage { field1 = content }
        val response = stub
            .simpleClientStreamingRpc(
                flow {
                    emit(message)
                    emit(message)
                    emit(message)
                }
            )

        assertEquals(content.repeat(3), response.field1)
    }

    @Test
    fun testBidiStream() = runTest {
        val message = simpleMessage { field1 = "test message" }
        val responses = stub
            .bidiStreamingRpc(
                flow {
                    emit(message)
                    emit(message)
                    emit(message)
                }
            )
            .toList()

        assertEquals(listOf(message, message, message), responses)
    }

    @Test
    fun testCannotStartClientStreamingRpcOnCancelledChannel() = runTest {
        val message = cancellationMessage {}

        val channel = channel
        channel.shutdown()

        assertFailsWithUnavailableOrCancelledStatus {
            CancellationServiceStub(channel)
                .respondAfter10SecClientStreaming(
                    flow {
                        emit(message)
                        emit(message)
                        emit(message)
                    }
                )
        }
    }

    @Test
    fun testCannotStartBidiStreamingRpcOnCancelledChannel() = runTest {
        val message = cancellationMessage {}

        val channel = channel
        channel.shutdown()

        assertFailsWithUnavailableOrCancelledStatus {
            CancellationServiceStub(channel)
                .pingPong(
                    flow {
                        emit(message)
                        emit(message)
                        emit(message)
                    }
                )
                .toList()
        }
    }

    @Test
    fun testClientStreamingRpcIsCancelledImmediatelyOnImmediateShutdown() = runTest {
        val message = cancellationMessage {}

        val channel = channel

        coroutineScope {
            launch {
                delay(1000)
                channel.shutdownNow()
            }

            assertFailsWithUnavailableOrCancelledStatus {
                CancellationServiceStub(channel)
                    .respondAfter10SecClientStreaming(
                        flow {
                            emit(message)
                            emit(message)
                            emit(message)
                        }
                    )
            }
        }
    }

    @Test
    fun testBidiStreamingRpcIsCancelledImmediatelyOnImmediateShutdown() = runTest {
        val message = cancellationMessage {}

        val channel = channel

        coroutineScope {
            launch {
                delay(1000)
                channel.shutdownNow()
            }

            assertFailsWithUnavailableOrCancelledStatus {
                CancellationServiceStub(channel)
                    .pingPong(
                        flow {
                            emit(message)
                            emit(message)
                            emit(message)
                        }
                    )
                    .toList()
            }
        }
    }
}
