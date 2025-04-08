package io.github.timortel.kmpgrpc.testserver

import io.github.timortel.kmpgrpc.test.*
import io.grpc.Server
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import io.grpc.protobuf.services.ProtoReflectionServiceV1


object TestServer {

    fun start(): Server {
        return NettyServerBuilder
            .forPort(17888)
            .addService(ProtoReflectionServiceV1.newInstance())
            .addService(object : TestServiceGrpcKt.TestServiceCoroutineImplBase() {
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
            .addService(object : UnknownFieldServiceGrpcKt.UnknownFieldServiceCoroutineImplBase() {
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
            })
            .build()
            .start()
    }
}
