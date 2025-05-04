package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kmpgrpc.core.Channel
import io.github.timortel.kmpgrpc.test.InterceptorMessage
import io.github.timortel.kmpgrpc.test.InterceptorServiceStub
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class NativeJvmInterceptorTest : InterceptorTest() {

    /**
     * Tests that the happens-before behavior is not violated for client side streaming calls
     */
    @Test
    fun testInterceptorInternalCallOrderClientStreaming() = runTest {
        val interceptor = CheckCallOrderInterceptor(isClientStream = true, isServerStream = false, isJs = false)

        val channel = Channel.Builder
            .forAddress(address, port)
            .usePlaintext()
            .withInterceptors(interceptor)
            .build()

        val message = InterceptorMessage(a = 1)

        InterceptorServiceStub(channel)
            .clientStream(flow {
                emit(message)
                emit(message)
                emit(message)
            })

        assertEquals(InterceptorLifecycleStatus.CLOSED, interceptor.lifecycleStatus)
    }

    /**
     * Tests that the happens-before behavior is not violated for bidi streaming calls
     */
    @Test
    fun testInterceptorInternalCallOrderBidiStreaming() = runTest {
        val interceptor = CheckCallOrderInterceptor(isClientStream = true, isServerStream = true, isJs = false)

        val channel = Channel.Builder
            .forAddress(address, port)
            .usePlaintext()
            .withInterceptors(interceptor)
            .build()

        val message = InterceptorMessage(a = 1)

        InterceptorServiceStub(channel)
            .bidiStream(flow {
                emit(message)
                emit(message)
                emit(message)
            })
            .toList()

        assertEquals(InterceptorLifecycleStatus.CLOSED, interceptor.lifecycleStatus)
    }
}
