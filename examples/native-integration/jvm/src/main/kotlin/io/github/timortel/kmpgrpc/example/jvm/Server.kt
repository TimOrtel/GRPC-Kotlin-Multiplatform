package io.github.timortel.kmpgrpc.example.jvm

import hello.HelloResponse
import hello.HelloServiceGrpcKt
import io.grpc.ServerBuilder

fun main() {
    ServerBuilder.forPort(17600)
        .addService(object : HelloServiceGrpcKt.HelloServiceCoroutineImplBase() {
            override suspend fun sayHello(request: hello.HelloRequest): HelloResponse {
                return HelloResponse.newBuilder()
                    .setResponse("Response from server to ${request.greeting}")
                    .build()
            }
        })
        .build()
        .start()
        .awaitTermination()
}