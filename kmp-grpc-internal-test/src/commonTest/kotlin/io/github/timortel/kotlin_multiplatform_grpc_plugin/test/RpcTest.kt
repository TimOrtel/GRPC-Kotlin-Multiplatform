package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kmpgrpc.core.KMChannel
import io.github.timortel.kmpgrpc.core.message.UnknownField
import io.github.timortel.kmpgrpc.test.*
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class RpcTest {

    abstract val address: String
    abstract val port: Int

    private val channel: KMChannel
        get() = KMChannel.Builder
            .forAddress(address, port)
            .usePlaintext()
            .build()

    private val stub: TestServiceStub get() = TestServiceStub(channel)

    @Test
    fun testEmpty() = runTest {
        try {
            val message = emptyMessage { }
            val response = stub
                .emptyRpc(message)

            assertEquals(message, response)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }

    }

    @Test
    fun testSimple() = runTest {
        val message = simpleMessage { field1 = "Test" }

        val response = async {
            stub.simpleRpc(message)
        }

        assertEquals(message, response.await())
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

        assertTrue { response.unknownFields.isNotEmpty() }
        assertTrue { response.unknownFields.count { it is UnknownField.Varint } == 1 }
        assertTrue { response.unknownFields.count { it is UnknownField.Fixed32 } == 1 }
        assertTrue { response.unknownFields.count { it is UnknownField.Fixed64 } == 1 }
        assertTrue { response.unknownFields.count { it is UnknownField.LengthDelimited } == 1 }

        assertEquals(13L, getUF<UnknownField.Varint>(response).value)
        assertEquals(-4f, Float.fromBits(getUF<UnknownField.Fixed32>(response).value.toInt()))
        assertEquals(64.0, Double.fromBits(getUF<UnknownField.Fixed64>(response).value.toLong()))
        assertEquals("Test Message", getUF<UnknownField.LengthDelimited>(response).value.decodeToString())
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

    private inline fun <reified T> getUF(m: Unknownfield.MessageWithUnknownField): T {
        return m.unknownFields.filterIsInstance<T>().first()
    }
}
