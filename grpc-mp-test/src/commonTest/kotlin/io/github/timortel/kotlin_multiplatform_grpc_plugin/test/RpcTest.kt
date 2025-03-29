package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kotlin_multiplatform_grpc_lib.KMChannel
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.basic_messages.*
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class RpcTest {

    abstract val address: String
    abstract val port: Int

    private val channel: KMChannel get() = KMChannel.Builder
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

            println(response)
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
    fun testStreamEverything() =  runTest {
        val message = createMessageWithAllTypes()
        val flow: Flow<MessageWithEverything> = stub
            .everythingStreamingRpc(message)

        assertEquals(listOf(message, message, message), flow.toList())
    }
}