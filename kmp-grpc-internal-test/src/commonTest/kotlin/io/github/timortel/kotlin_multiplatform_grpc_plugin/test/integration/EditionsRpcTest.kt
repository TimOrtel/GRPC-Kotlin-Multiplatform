package io.github.timortel.kotlin_multiplatform_grpc_plugin.test.integration

import io.github.timortel.kmpgrpc.core.Channel
import io.github.timortel.kmpgrpc.test.EditionsTestServiceStub
import io.github.timortel.kmpgrpc.test.editions.EditionsLegacyField
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.createEditionsNonPackedTypesMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.createEditionsPackedTypesMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.createMessageWithAllExtensions
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class EditionsRpcTest : ServerTest {

    protected val channel: Channel
        get() = Channel.Builder
            .forAddress(address, port)
            .usePlaintext()
            .build()

    protected val stub: EditionsTestServiceStub get() = EditionsTestServiceStub(channel)

    @Test
    fun testEditionsPackedTypes() = runTest {
        val message = createEditionsPackedTypesMessage()
        val response = stub.sendPacked(message)

        assertEquals(message, response)
    }

    @Test
    fun testEditionsNonPackedTypes() = runTest {
        val message = createEditionsNonPackedTypesMessage()
        val response = stub.sendExpanded(message)

        assertEquals(message, response)
    }

    @Test
    fun testLegacyRequiredFieldNoData() = runTest {
        val message = EditionsLegacyField()
        val response = stub.sendLegacyRequiredField(message)

        assertEquals(message, response)
    }

    @Test
    fun testLegacyRequiredFieldData() = runTest {
        val message = EditionsLegacyField(a = 13)
        val response = stub.sendLegacyRequiredField(message)

        assertEquals(message, response)
    }

    @Test
    fun testMessageWithAllExtensions() = runTest {
        val message = createMessageWithAllExtensions()
        val response = stub.sendMessageWithEveryExtension(message)

        assertEquals(message, response)
    }
}
