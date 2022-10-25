package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kotlin_multiplatform_grpc_lib.KMChannel
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.basic_messages.KMMessageWithEverything
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.basic_messages.KMSimpleMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.basic_messages.KMTestServiceStub
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.basic_messages.kmSimpleMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RpcTest {

    private val channel = KMChannel.Builder
        .forAddress("localhost", 17888)
        .usePlaintext()
        .build()

    private val stub = KMTestServiceStub(channel)

    @Test
    fun testSimple() {
        runTest {
            val message = kmSimpleMessage { field1 = "Test" }
            val response = stub
                .simpleRpc(message)

            assertEquals(message, response)
        }
    }

    @Test
    fun testScalar() {
        runTest {
            val message = createScalarMessage()
            val response = stub
                .scalarRpc(message)

            assertEquals(message, response)
        }
    }

    @Test
    fun testEverything() {
        runTest {
            val message = createMessageWithAllTypes()
            val response = stub
                .everythingRpc(message)

            assertEquals(message, response)
        }
    }

    @Test
    fun testStreamSimple() {
        runTest {
            val message = kmSimpleMessage { field1 = "Streaming test" }
            val flow: Flow<KMSimpleMessage> = stub
                .simpleStreamingRpc(message)

            assertEquals(listOf(message, message, message), flow.toList())
        }
    }

    @Test
    fun testStreamEverything() {
        runTest {
            val message = createMessageWithAllTypes()
            val flow: Flow<KMMessageWithEverything> = stub
                .everythingStreamingRpc(message)

            assertEquals(listOf(message, message, message), flow.toList())
        }
    }
}