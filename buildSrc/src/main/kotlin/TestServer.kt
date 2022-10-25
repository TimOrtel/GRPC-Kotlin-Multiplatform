import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.basic_messages.*
import io.grpc.Server
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


object TestServer {

    private var server: Server? = null

    fun start() {
        if (server != null) return

        server = NettyServerBuilder
            .forPort(17888)
            .addService(object : TestServiceGrpcKt.TestServiceCoroutineImplBase() {
                override suspend fun simpleRpc(request: SimpleMessage): SimpleMessage {
                    return request
                }

                override suspend fun scalarRpc(request: ScalarTypes): ScalarTypes {
                    return request
                }

                override suspend fun everythingRpc(request: MessageWithEverything): MessageWithEverything {
                    return request
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
    }

    fun stop() {
        server?.shutdownNow()
        server = null
    }
}