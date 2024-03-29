package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kotlin_multiplatform_grpc_lib.KMChannel
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.basic_messages.*
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.fail

abstract class RpcTest {

    abstract val address: String
    abstract val port: Int

    private val channel: KMChannel get() = KMChannel.Builder
        .forAddress(address, port)
        .usePlaintext()
        .build()

    private val stub: KMTestServiceStub get() = KMTestServiceStub(channel)

    @Test
    fun testEmpty() = runTest {
        try {
            val message = kmEmptyMessage { }
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
        val message = kmSimpleMessage { field1 = "Test" }

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
        val message = kmEmptyMessage { }
        val flow: Flow<KMEmptyMessage> = stub
            .emptyStream(message)

        assertEquals(listOf(message, message, message), flow.toList())
    }

    @Test
    fun testStreamSimple() = runTest {
        val message = kmSimpleMessage { field1 = "Streaming test" }
        val flow: Flow<KMSimpleMessage> = stub
            .simpleStreamingRpc(message)

        assertEquals(listOf(message, message, message), flow.toList())
    }

    @Test
    fun testStreamEverything() =  runTest {
        val message = createMessageWithAllTypes()
        val flow: Flow<KMMessageWithEverything> = stub
            .everythingStreamingRpc(message)

        assertEquals(listOf(message, message, message), flow.toList())
    }
}