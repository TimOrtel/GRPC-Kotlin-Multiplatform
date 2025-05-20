package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kmpgrpc.core.CallInterceptor
import io.github.timortel.kmpgrpc.core.Channel
import io.github.timortel.kmpgrpc.core.MethodDescriptor
import io.github.timortel.kmpgrpc.core.metadata.Entry
import io.github.timortel.kmpgrpc.core.metadata.Key
import io.github.timortel.kmpgrpc.core.metadata.Metadata
import io.github.timortel.kmpgrpc.test.InterceptorServiceStub
import io.github.timortel.kmpgrpc.test.interceptorMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class MetadataTest : ServerTest {

    protected val channel: Channel
        get() = Channel.Builder
            .forAddress(address, port)
            .usePlaintext()
            .build()

    protected val stub: InterceptorServiceStub get() = InterceptorServiceStub(channel)

    @Test
    fun testCanSendAsciiMetadata() = runTest {
        val message = interceptorMessage { }

        val key = "custom-header-1"
        val value = "custom-value"

        val response = stub
            .testMetadata(message, Metadata.of(Key.AsciiKey(key), value))

        assertContains(response.metadataMap, key, "Metadata does not contain key $key")
        assertEquals(value, response.metadataMap[key], "Metadata value != $value")
    }

    @Test
    fun testCanSendMultiAsciiMetadata() = runTest {
        val message = interceptorMessage { }

        val key = "custom-header-1"
        val values = setOf("custom-value1", "custom-value2")

        val response = stub
            .testMetadata(message, Metadata.of(Entry.Ascii(Key.AsciiKey(key), values)))

        assertContains(response.metadataMap, key, "Metadata does not contain key $key")
        assertEquals(values.joinToString(), response.metadataMap[key], "Metadata does not contain expected values")
    }

    @Test
    fun testCanSendBinaryMetadata() = runTest {
        val message = interceptorMessage { }

        val key = "custom-header-1-bin"
        val value = "custom-value"

        val response = stub
            .testMetadata(message, Metadata.of(Key.BinaryKey(key), value.encodeToByteArray()))

        assertContains(response.metadataMap, key, "Metadata does not contain key $key")
        assertEquals(value, response.metadataMap[key], "Metadata value != $value")
    }

    @Test
    fun testCanSendMultiBinaryMetadata() = runTest {
        val message = interceptorMessage { }

        val key = "custom-header-1-bin"
        val values = setOf("custom-value1", "custom-value2")

        val valuesEncoded = values.map { it.encodeToByteArray() }.toSet()

        val response = stub
            .testMetadata(message, Metadata.of(Entry.Binary(Key.BinaryKey(key), valuesEncoded)))

        assertContains(response.metadataMap, key, "Metadata does not contain key $key")
        assertEquals(values.joinToString(), response.metadataMap[key], "Metadata does not contain expected values")
    }

    @Test
    fun testCanReceiveMultiMetadata() = runTest {
        val headers = MutableStateFlow<Metadata?>(null)

        val interceptor = object : CallInterceptor {
            override fun onReceiveHeaders(methodDescriptor: MethodDescriptor, metadata: Metadata): Metadata {
                headers.value = metadata
                return metadata
            }
        }

        val channel = Channel.Builder
            .forAddress(address, port)
            .usePlaintext()
            .withInterceptors(interceptor)
            .build()

        InterceptorServiceStub(channel)
            .testMetadata(interceptorMessage { })

        val receivedHeaders = headers.filterNotNull().first()

        val asciiValues = receivedHeaders.getAll(Key.AsciiKey("custom-header-1"))
        assertContains(asciiValues, "value1", "Expected ascii values to contain value1")
        assertContains(asciiValues, "value2", "Expected ascii values to contain value2")

        val binaryValues = receivedHeaders.getAll(Key.BinaryKey("custom-header-1-bin"))

        assertContainsByteArray(binaryValues, "value1".encodeToByteArray(), "Expected binary values to contain value1")
        assertContainsByteArray(binaryValues, "value2".encodeToByteArray(), "Expected binary values to contain value2")
    }

    private fun assertContainsByteArray(arrays: Set<ByteArray>, element: ByteArray, message: String) {
        assertTrue(message) {
            arrays.any { it.contentEquals(element) }
        }
    }
}
