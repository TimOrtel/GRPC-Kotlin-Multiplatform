package io.github.timortel.kmpgrpc.testserver

import io.github.timortel.kmpgrpc.test.*
import io.grpc.*
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import io.grpc.protobuf.services.ProtoReflectionServiceV1
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds


object TestServer {

    fun start(): Server {
        val metadataKey = Context.key<Map<String, String>>("metadata")

        return NettyServerBuilder
            .forPort(17888)
            .permitKeepAliveTime(10, TimeUnit.SECONDS)
            .addService(ProtoReflectionServiceV1.newInstance())
            .addService(
                object : TestServiceGrpcKt.TestServiceCoroutineImplBase() {
                    override suspend fun emptyRpc(request: EmptyMessage): EmptyMessage {
                        return request
                    }

                    override suspend fun simpleRpc(request: SimpleMessage): SimpleMessage {
                        return request
                    }

                    override suspend fun scalarRpc(request: ScalarTypes): ScalarTypes {
                        return request
                    }

                    override suspend fun everythingRpc(request: MessageWithEverything): MessageWithEverything {
                        return request
                    }

                    override fun emptyStream(request: EmptyMessage): Flow<EmptyMessage> {
                        return flow {
                            emit(request)
                            emit(request)
                            emit(request)
                        }
                    }

                    override fun simpleStreamingRpc(request: SimpleMessage): Flow<SimpleMessage> {
                        return flow {
                            emit(request)
                            emit(request)
                            emit(request)
                        }
                    }

                    override fun everythingStreamingRpc(request: MessageWithEverything): Flow<MessageWithEverything> {
                        return flow {
                            emit(request)
                            emit(request)
                            emit(request)
                        }
                    }

                    override suspend fun simpleClientStreamingRpc(requests: Flow<SimpleMessage>): SimpleMessage {
                        val responseContent = requests.toList().joinToString(separator = "") { it.field1 }

                        return simpleMessage { field1 = responseContent }
                    }

                    override fun bidiStreamingRpc(requests: Flow<SimpleMessage>): Flow<SimpleMessage> {
                        return requests
                    }

                    override suspend fun unaryDelayed(request: SimpleMessage): SimpleMessage {
                        delay(500)
                        return request
                    }

                    override fun serverStreamingDelayed(request: SimpleMessage): Flow<SimpleMessage> {
                        return flow {
                            emit(request)
                            delay(500)
                            emit(request)
                        }
                    }
                }
            )
            .addService(
                object : UnknownFieldServiceGrpcKt.UnknownFieldServiceCoroutineImplBase() {
                    override suspend fun fillWithUnknownFields(request: Unknownfield.MessageWithUnknownField): Unknownfield.MessageWithUnknownField {
                        return request.copy {
                            unknownVarInt = 13
                            unknownFixed32 = -4f
                            unknownFixed64 = 64.0
                            unknownLengthDelimited = "Test Message"
                        }
                    }

                    override suspend fun returnIdentically(request: Unknownfield.MessageWithUnknownField): Unknownfield.MessageWithUnknownField {
                        return request
                    }
                }
            )
            .addService(
                object : CancellationServiceGrpcKt.CancellationServiceCoroutineImplBase() {
                    override suspend fun respondAfter10Sec(request: CancellationMessage): CancellationResponse {
                        delay(10.seconds)
                        return cancellationResponse { }
                    }

                    override fun respondImmediatelyAndAfter10Sec(request: CancellationMessage): Flow<CancellationResponse> {
                        return flow {
                            emit(cancellationResponse { })
                            delay(10.seconds)
                            emit(cancellationResponse { })
                        }
                    }

                    override suspend fun respondAfter10SecClientStreaming(requests: Flow<CancellationMessage>): CancellationResponse {
                        delay(10.seconds)
                        return cancellationResponse { }
                    }

                    override fun pingPong(requests: Flow<CancellationMessage>): Flow<CancellationResponse> {
                        return requests.map { cancellationResponse { } }
                    }
                }
            )
            .addService(
                object : InterceptorServiceGrpcKt.InterceptorServiceCoroutineImplBase() {
                    override suspend fun send(request: InterceptorMessage): InterceptorMessage {
                        return request
                    }

                    override fun receiveStream(request: InterceptorMessage): Flow<InterceptorMessage> {
                        return flow {
                            emit(request)
                            emit(request)
                            emit(request)
                        }
                    }

                    override suspend fun testMetadata(request: InterceptorMessage): MetadataMessage {
                        val metadata = metadataKey.get(Context.current())

                        return metadataMessage {
                            this.metadata.putAll(metadata)
                        }
                    }

                    override suspend fun clientStream(requests: Flow<InterceptorMessage>): InterceptorMessage {
                        return requests.last()
                    }

                    override fun bidiStream(requests: Flow<InterceptorMessage>): Flow<InterceptorMessage> {
                        return requests
                    }

                    override suspend fun testReceiveMetadata(request: InterceptorMessage): InterceptorMessage {
                        return request
                    }
                }
            )
            .intercept(
                object : ServerInterceptor {
                    override fun <ReqT : Any?, RespT : Any?> interceptCall(
                        call: ServerCall<ReqT, RespT>?,
                        headers: Metadata,
                        next: ServerCallHandler<ReqT, RespT>?
                    ): ServerCall.Listener<ReqT> {
                        val map = buildMap<String, String> {
                            headers.keys().orEmpty().forEach { key ->
                                val values = if (key.endsWith("-bin")) {
                                    headers.getAll(Metadata.Key.of(key, Metadata.BINARY_BYTE_MARSHALLER))
                                        ?.toList()
                                        .orEmpty()
                                        .joinToString { it.decodeToString() }
                                } else {
                                    val headers = headers.getAll(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER))
                                    headers?.toList().orEmpty()
                                        .joinToString()
                                }

                                put(key, values)
                            }
                        }

                        val context = Context.current().withValue(metadataKey, map)
                        return Contexts.interceptCall(context, CustomResponseCall(call!!), headers, next)
                    }
                }
            )
            .build()
            .start()
    }

    private class CustomResponseCall<R, S>(delegate: ServerCall<R, S>) :
        ForwardingServerCall.SimpleForwardingServerCall<R, S>(delegate) {

        override fun sendHeaders(headers: Metadata) {
            val asciiKey = Metadata.Key.of("custom-header-1", Metadata.ASCII_STRING_MARSHALLER)
            headers.put(asciiKey, "value1")
            headers.put(asciiKey, "value2")

            val binaryKey = Metadata.Key.of("custom-header-1-bin", Metadata.BINARY_BYTE_MARSHALLER)
            headers.put(binaryKey, "value1".encodeToByteArray())
            headers.put(binaryKey, "value2".encodeToByteArray())

            super.sendHeaders(headers)
        }
    }
}
