package io.github.timortel.grpc_multiplatform.jvm

import io.grpc.ServerBuilder
import io.github.timortel.grpc_multiplatform.example.HelloServiceGrpcKt;
import io.github.timortel.grpc_multiplatform.example.HelloRequest;
import io.github.timortel.grpc_multiplatform.example.Response;
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun main() {
    ServerBuilder.forPort(17600)
        .addService(object : HelloServiceGrpcKt.HelloServiceCoroutineImplBase() {
            override suspend fun sayHello(request: HelloRequest): Response {
                return Response.newBuilder()
                    .setResponse("Response from server to ${request.greeting}")
                    .build()
            }

            override fun sayHelloMultipleTimes(request: HelloRequest): Flow<Response> {
                return flow {
                    for (i in 0 until 5) {
                        emit(
                            Response.newBuilder()
                                .setResponse("Response from server to ${request.greeting}; $i")
                                .build()
                        )
                        delay(1000)
                    }
                }
            }
        })
        .build()
        .start()
        .awaitTermination()
}