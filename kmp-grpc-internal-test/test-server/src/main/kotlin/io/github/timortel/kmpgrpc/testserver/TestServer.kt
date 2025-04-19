package io.github.timortel.kmpgrpc.testserver

import io.github.timortel.kmpgrpc.test.*
import io.grpc.*
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import io.grpc.protobuf.services.ProtoReflectionServiceV1
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.seconds


object TestServer {

    fun start(): Server {
        val metadataKey = Context.key<Map<String, String>>("metadata")

        return NettyServerBuilder
            .forPort(17888)
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
                        return cancellationResponse {  }
                    }

                    override fun respondImmediatelyAndAfter10Sec(request: CancellationMessage): Flow<CancellationResponse> {
                        return flow {
                            emit(cancellationResponse {  })
                            delay(10.seconds)
                            emit(cancellationResponse {  })
                        }
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
                                put(key, headers.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER)).orEmpty())
                            }
                        }

                        val context = Context.current().withValue(metadataKey, map)
                        return Contexts.interceptCall(context, call, headers, next)
                    }
                }
            )
            .build()
            .start()
    }
}
