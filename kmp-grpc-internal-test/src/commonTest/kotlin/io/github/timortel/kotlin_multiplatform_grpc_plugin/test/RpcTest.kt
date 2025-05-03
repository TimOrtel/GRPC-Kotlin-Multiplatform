package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kmpgrpc.core.Channel
import io.github.timortel.kmpgrpc.core.Code
import io.github.timortel.kmpgrpc.core.Metadata
import io.github.timortel.kmpgrpc.core.StatusException
import io.github.timortel.kmpgrpc.core.message.UnknownField
import io.github.timortel.kmpgrpc.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

abstract class RpcTest {

    abstract val address: String
    abstract val port: Int

    protected val channel: Channel
        get() = Channel.Builder
            .forAddress(address, port)
            .usePlaintext()
            .build()

    protected val stub: TestServiceStub get() = TestServiceStub(channel)

    @Test
    fun testEmpty() = runTest {
        val message = emptyMessage { }
        val response = stub
            .emptyRpc(message)

        assertEquals(message, response)
    }

    @Test
    fun testSimple() = runTest {
        val message = simpleMessage { field1 = "Test" }

        val response = stub.simpleRpc(message)

        assertEquals(message, response)
    }

    @Test
    fun testScalar() = runTest {
        val message = createScalarMessage()
        val response = stub
            .scalarRpc(message)

        assertEquals(message, response)
    }

    @Test
    fun testEverything() = runTest {
        val message = createMessageWithAllTypes()
        val response = stub
            .everythingRpc(message)

        assertEquals(message, response)
    }

    @Test
    fun testStreamEmpty() = runTest {
        val message = emptyMessage { }
        val flow: Flow<EmptyMessage> = stub
            .emptyStream(message)

        assertEquals(listOf(message, message, message), flow.toList())
    }

    @Test
    fun testStreamSimple() = runTest {
        val message = simpleMessage { field1 = "Streaming test" }
        val flow: Flow<SimpleMessage> = stub
            .simpleStreamingRpc(message)

        assertEquals(listOf(message, message, message), flow.toList())
    }

    @Test
    fun testStreamEverything() = runTest {
        val message = createMessageWithAllTypes()
        val flow: Flow<MessageWithEverything> = stub
            .everythingStreamingRpc(message)

        assertEquals(listOf(message, message, message), flow.toList())
    }

    @Test
    fun testReceiveUnknownFields() = runTest {
        val message = messageWithUnknownField {
            a = "WWW"
        }

        val response = Unknownfield.UnknownFieldServiceStub(channel)
            .fillWithUnknownFields(message)

        val varint = UnknownField.Varint(2, 13L)
        val fixed32 = UnknownField.Fixed32(3, (-4f).toBits().toUInt())
        val fixed64 = UnknownField.Fixed64(4, (64.0).toBits().toULong())
        val ld = UnknownField.LengthDelimited(5, "Test Message".encodeToByteArray())

        assertEquals(4, response.unknownFields.size)
        assertContains(response.unknownFields, varint)
        assertContains(response.unknownFields, fixed32)
        assertContains(response.unknownFields, fixed64)
        assertContains(response.unknownFields, ld)
    }

    @Test
    fun testSendUnknownFields() = runTest {
        val message = messageWithUnknownField {
            a = "WWW"
        }

        val stub = Unknownfield.UnknownFieldServiceStub(channel)
        val baseMessage = stub.fillWithUnknownFields(message)

        val returnedMessage = stub.returnIdentically(baseMessage)

        assertEquals(baseMessage, returnedMessage)
    }

    @Test
    fun testCanSendMetadata() = runTest {
        val message = interceptorMessage { }

        val key = "custom-header-1"
        val value = "custom-value"

        val response = InterceptorServiceStub(channel)
            .testMetadata(message, Metadata.of(key, value))

        assertContains(response.metadataMap, key, "Metadata does not contain key $key")
        assertEquals(value, response.metadataMap[key], "Metadata value != $value")
    }

    @Test
    fun testFailedRpcThrowsKmStatusException() = runTest {
        val message = simpleMessage { }

        assertFailsWith<StatusException> {
            TestServiceStub(Channel.Builder.forAddress("localhost", 1800).usePlaintext().build())
                .simpleRpc(message)
        }
    }

    @Test
    fun testFailedStreamingRpcThrowsKmStatusException() = runTest {
        val message = simpleMessage { }

        assertFailsWith<StatusException> {
            TestServiceStub(Channel.Builder.forAddress("localhost", 1800).usePlaintext().build())
                .simpleStreamingRpc(message)
                .toList()
        }
    }

    @Test
    fun testCannotStartUnaryRpcOnCancelledChannel() = runTest {
        val message = cancellationMessage {}

        val channel = channel
        channel.shutdown()

        assertFailsWithUnavailableOrCancelledStatus {
            CancellationServiceStub(channel)
                .respondAfter10Sec(message)
        }
    }

    @Test
    fun testCannotStartServerStreamingRpcOnCancelledChannel() = runTest {
        val message = cancellationMessage {}

        val channel = channel
        channel.shutdown()

        assertFailsWithUnavailableOrCancelledStatus {
            CancellationServiceStub(channel)
                .respondImmediatelyAndAfter10Sec(message)
                .toList()
        }
    }

    @Test
    fun testUnaryRpcIsCancelledImmediatelyOnImmediateShutdown() = runTest {
        val message = cancellationMessage {}

        val channel = channel

        coroutineScope {
            launch {
                withContext(Dispatchers.Default) {
                    delay(1000)
                }

                channel.shutdownNow()
            }

            assertFailsWithUnavailableOrCancelledStatus {
                CancellationServiceStub(channel)
                    .respondAfter10Sec(message)
            }
        }
    }

    @Test
    fun testServerStreamingRpcIsCancelledImmediatelyOnImmediateShutdown() = runTest {
        val message = cancellationMessage {}

        val channel = channel

        coroutineScope {
            launch {
                withContext(Dispatchers.Default) {
                    delay(1000)
                }

                channel.shutdownNow()
            }

            val receivedResponses = mutableListOf<CancellationResponse>()

            assertFailsWithUnavailableOrCancelledStatus {
                CancellationServiceStub(channel)
                    .respondImmediatelyAndAfter10Sec(message)
                    .collect { receivedResponses += it }
            }

            assertEquals(1, receivedResponses.size, "Expected to have received exactly 1 response.")
        }
    }

    @Test
    fun testUnaryDeadlineTriggered() = runTest {
        assertFailsWithTimeoutStatus {
            withContext(Dispatchers.Default) {
                stub
                    .withDeadlineAfter(200.milliseconds)
                    .unaryDelayed(simpleMessage { })
            }
        }
    }

    @Test
    fun testUnaryDeadlineNotTriggered() = runTest {
        withContext(Dispatchers.Default) {
            stub
                .withDeadlineAfter(1.seconds)
                .simpleRpc(simpleMessage { field1 = "Test" })
        }
    }

    @Test
    fun testServerStreamingDeadlineTriggered() = runTest {
        val received = mutableListOf<SimpleMessage>()

        assertFailsWithTimeoutStatus {
            withContext(Dispatchers.Default) {
                stub
                    .withDeadlineAfter(200.milliseconds)
                    .serverStreamingDelayed(simpleMessage { })
                    .toList(received)
            }
        }

        assertEquals(1, received.size, "Expected to have received exactly 1 message.")
    }

    @Test
    fun testServerStreamingDeadlineNotTriggered() = runTest {
        withContext(Dispatchers.Default) {
            val received = stub
                .withDeadlineAfter(1.seconds)
                .serverStreamingDelayed(simpleMessage { })
                .toList()

            assertEquals(2, received.size, "Expected to have received 2 messages.")
        }
    }

    @Test
    fun testUnimplementedRpcThrowsStatusException() = runTest {
        assertFailsWithCode(listOf(Code.UNIMPLEMENTED)) {
            stub
                .unaryUnimplemented(simpleMessage { })
        }
    }

    protected inline fun assertFailsWithUnavailableOrCancelledStatus(block: () -> Unit) {
        assertFailsWithCode(listOf(Code.UNAVAILABLE, Code.CANCELLED), block)
    }

    protected inline fun assertFailsWithTimeoutStatus(block: () -> Unit) {
        assertFailsWithCode(listOf(Code.DEADLINE_EXCEEDED), block)
    }

    protected inline fun assertFailsWithCode(codes: List<Code>, block: () -> Unit) {
        val exception = assertFailsWith<StatusException> { block() }

        assertContains(
            codes,
            exception.status.code,
            "Expected to fail with $codes status. statusMessage=${exception.status.statusMessage}"
        )
    }
}

