package io.github.timortel.kmpgrpc.testserver

import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.basic_messages.*
import io.grpc.Server
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import io.grpc.protobuf.services.ProtoReflectionServiceV1;


object TestServer {

    private var server: Server? = null

    fun start(): Server {
        val currentServer = server
        if (currentServer != null) return currentServer

        val createdServer = NettyServerBuilder
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
            .build()
            .start()

        server = createdServer

        return createdServer
    }

    fun stop() {
        server?.shutdownNow()
        server = null
    }
}
