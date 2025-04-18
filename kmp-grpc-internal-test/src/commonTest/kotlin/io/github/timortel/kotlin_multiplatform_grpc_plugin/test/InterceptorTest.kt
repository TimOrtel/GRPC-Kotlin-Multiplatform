package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kmpgrpc.core.*
import io.github.timortel.kmpgrpc.core.message.Message
import io.github.timortel.kmpgrpc.test.InterceptorMessage
import io.github.timortel.kmpgrpc.test.InterceptorServiceStub
import io.github.timortel.kmpgrpc.test.interceptorMessage
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import kotlin.test.*

abstract class InterceptorTest {

    abstract val address: String
    abstract val port: Int

    abstract val isJavaScript: Boolean

    private val basicInterceptor = object : CallInterceptor {
        override fun <T : Message> onReceiveMessage(methodDescriptor: MethodDescriptor, message: T): T {
            assertIs<InterceptorMessage>(message)
            @Suppress("UNCHECKED_CAST")
            return message.copy(a = message.a + 1) as T
        }

        override fun <T : Message> onSendMessage(methodDescriptor: MethodDescriptor, message: T): T {
            assertIs<InterceptorMessage>(message)
            @Suppress("UNCHECKED_CAST")
            return message.copy(a = message.a + 1) as T
        }
    }

    // Reverse order on sending events

    private val sendInterceptor1 = object : CallInterceptor {
        override fun <T : Message> onSendMessage(methodDescriptor: MethodDescriptor, message: T): T {
            assertIs<InterceptorMessage>(message)
            @Suppress("UNCHECKED_CAST")
            return message.copy(a = message.a + 1) as T
        }
    }

    private val sendInterceptor2 = object : CallInterceptor {
        override fun <T : Message> onSendMessage(methodDescriptor: MethodDescriptor, message: T): T {
            assertIs<InterceptorMessage>(message)
            @Suppress("UNCHECKED_CAST")
            return message.copy(a = message.a * 5) as T
        }
    }

    // Normal order on receiving events

    private val receiveInterceptor1 = object : CallInterceptor {
        override fun <T : Message> onReceiveMessage(methodDescriptor: MethodDescriptor, message: T): T {
            assertIs<InterceptorMessage>(message)
            @Suppress("UNCHECKED_CAST")
            return message.copy(a = message.a * 5) as T
        }
    }

    private val receiveInterceptor2 = object : CallInterceptor {
        override fun <T : Message> onReceiveMessage(methodDescriptor: MethodDescriptor, message: T): T {
            assertIs<InterceptorMessage>(message)
            @Suppress("UNCHECKED_CAST")
            return message.copy(a = message.a + 1) as T
        }
    }

    /**
     * Tests that [basicInterceptor] is called successfully.
     */
    @Test
    fun testInterceptorCalled() = runTest {
        testResponse(3, arrayOf(basicInterceptor))
    }

    /**
     * Tests that [sendInterceptor1] is called before [sendInterceptor2]
     */
    @Test
    fun testInterceptorOrderSend() = runTest {
        testResponse(10, arrayOf(sendInterceptor2, sendInterceptor1))
    }

    /**
     * Tests that [sendInterceptor1] is called before [sendInterceptor2]
     */
    @Test
    fun testInterceptorOrderReceive() = runTest {
        testResponse(10, arrayOf(receiveInterceptor2, receiveInterceptor1))
    }

    private suspend fun testResponse(expectedValue: Int, interceptors: Array<CallInterceptor>) {
        val channel = Channel.Builder
            .forAddress(address, port)
            .usePlaintext()
            .withInterceptors(*interceptors)
            .build()

        val stub = InterceptorServiceStub(channel)
        val message = InterceptorMessage(a = 1)
        val response = stub
            .send(message)

        assertEquals(expectedValue, response.a)

        val stream = stub.receiveStream(message).toList()
        assertEquals(3, stream.size)
        stream.forEach { mes -> assertEquals(expectedValue, mes.a) }
    }

    /**
     * Tests that the happens-before behavior is not violated for unary calls
     */
    @Test
    fun testInterceptorInternalCallOrderUnary() = runTest {
        val interceptor = CheckCallOrderInterceptor(isStream = false, isJs = isJavaScript)

        val channel = Channel.Builder
            .forAddress(address, port)
            .usePlaintext()
            .withInterceptors(interceptor)
            .build()

        InterceptorServiceStub(channel)
            .send(InterceptorMessage(a = 1))

        assertEquals(InterceptorLifecycleStatus.CLOSED, interceptor.lifecycleStatus)
    }

    /**
     * Tests that the happens-before behavior is not violated for server side streaming calls
     */
    @Test
    fun testInterceptorInternalCallOrderServerStreaming() = runTest {
        val interceptor = CheckCallOrderInterceptor(isStream = true, isJs = isJavaScript)

        val channel = Channel.Builder
            .forAddress(address, port)
            .usePlaintext()
            .withInterceptors(interceptor)
            .build()

        InterceptorServiceStub(channel)
            .receiveStream(InterceptorMessage(a = 1))
            .toList()

        assertEquals(InterceptorLifecycleStatus.CLOSED, interceptor.lifecycleStatus)
    }

    @Test
    fun testCanManipulateSendingMetadata(): TestResult = runTest {
        val key = "custom-header-1"
        val value = "test-value"

        val interceptor = object : CallInterceptor {
            override fun onStart(methodDescriptor: MethodDescriptor, metadata: Metadata): Metadata {
                return super.onStart(methodDescriptor, metadata.withEntry(key, value))
            }
        }

        val channel = Channel.Builder
            .forAddress(address, port)
            .usePlaintext()
            .withInterceptors(interceptor)
            .build()

        val response = InterceptorServiceStub(channel)
            .testMetadata(interceptorMessage { })

        assertContains(response.metadataMap, key)
        assertEquals(value, response.metadataMap[key])
    }

    @Test
    fun testCanManipulateStatus(): TestResult = runTest {
        val message = "Set by interceptor"

        val interceptor = object : CallInterceptor {
            override fun onClose(
                methodDescriptor: MethodDescriptor,
                status: Status,
                metadata: Metadata
            ): Pair<Status, Metadata> {
                return super.onClose(methodDescriptor, Status(Code.UNKNOWN, message), metadata)
            }
        }

        val channel = Channel.Builder
            .forAddress(address, port)
            .usePlaintext()
            .withInterceptors(interceptor)
            .build()

        val exception = assertFailsWith<StatusException> {
            InterceptorServiceStub(channel)
                .send(interceptorMessage { })
        }

        assertEquals(Code.UNKNOWN, exception.status.code)
        assertContains(exception.status.statusMessage, message)
    }

    private enum class InterceptorLifecycleStatus {
        INIT,
        STARTED,
        SENT_MESSAGE,
        RECEIVED_HEADERS,
        RECEIVED_MESSAGE,
        CLOSED
    }

    private class CheckCallOrderInterceptor(val isStream: Boolean, val isJs: Boolean) : CallInterceptor {

        var lifecycleStatus: InterceptorLifecycleStatus = InterceptorLifecycleStatus.INIT

        override fun onStart(methodDescriptor: MethodDescriptor, metadata: Metadata): Metadata {
            if (lifecycleStatus == InterceptorLifecycleStatus.INIT) lifecycleStatus = InterceptorLifecycleStatus.STARTED
            else throw IllegalStateException()

            return super.onStart(methodDescriptor, metadata)
        }

        override fun <T : Message> onSendMessage(methodDescriptor: MethodDescriptor, message: T): T {
            if (lifecycleStatus == InterceptorLifecycleStatus.STARTED) lifecycleStatus =
                InterceptorLifecycleStatus.SENT_MESSAGE
            else throw IllegalStateException()

            return super.onSendMessage(methodDescriptor, message)
        }

        override fun onReceiveHeaders(methodDescriptor: MethodDescriptor, metadata: Metadata): Metadata {
            if (lifecycleStatus == InterceptorLifecycleStatus.SENT_MESSAGE) lifecycleStatus =
                InterceptorLifecycleStatus.RECEIVED_HEADERS
            else throw IllegalStateException()

            return super.onReceiveHeaders(methodDescriptor, metadata)
        }

        override fun <T : Message> onReceiveMessage(methodDescriptor: MethodDescriptor, message: T): T {
            when {
                isJs && isStream && lifecycleStatus == InterceptorLifecycleStatus.SENT_MESSAGE -> {
                    lifecycleStatus = InterceptorLifecycleStatus.RECEIVED_MESSAGE
                }

                lifecycleStatus == InterceptorLifecycleStatus.RECEIVED_HEADERS -> {
                    lifecycleStatus = InterceptorLifecycleStatus.RECEIVED_MESSAGE
                }

                isStream && lifecycleStatus == InterceptorLifecycleStatus.RECEIVED_MESSAGE -> {}

                else -> throw IllegalStateException()
            }

            return super.onReceiveMessage(methodDescriptor, message)
        }

        override fun onClose(
            methodDescriptor: MethodDescriptor,
            status: Status,
            metadata: Metadata
        ): Pair<Status, Metadata> {
            if (lifecycleStatus == InterceptorLifecycleStatus.RECEIVED_MESSAGE) lifecycleStatus =
                InterceptorLifecycleStatus.CLOSED
            else throw IllegalStateException(lifecycleStatus.toString())

            return super.onClose(methodDescriptor, status, metadata)
        }
    }
}
