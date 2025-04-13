package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kmpgrpc.core.CallInterceptor
import io.github.timortel.kmpgrpc.core.KMChannel
import io.github.timortel.kmpgrpc.core.KMMethodDescriptor
import io.github.timortel.kmpgrpc.core.message.KMMessage
import io.github.timortel.kmpgrpc.test.InterceptorMessage
import kotlin.test.Test
import kotlin.test.assertIs

abstract class InterceptorTest {

    abstract val address: String
    abstract val port: Int

    private val interceptor1 = object : CallInterceptor {
        override fun <T : KMMessage> onSendMessage(methodDescriptor: KMMethodDescriptor, message: T): T {
            assertIs<InterceptorMessage>(message)
            return message
        }

        override fun <T : KMMessage> onReceiveMessage(methodDescriptor: KMMethodDescriptor, message: T): T {
            return super.onReceiveMessage(methodDescriptor, message)
        }
    }

    private val channel: KMChannel
        get() = KMChannel.Builder
            .forAddress(address, port)
            .usePlaintext()
            .withInterceptors()
            .build()

    @Test
    fun test() {

    }
}