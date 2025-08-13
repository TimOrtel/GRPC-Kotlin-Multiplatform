package io.github.timortel.kmpgrpc

import hello.Hello
import hello.helloRequest
import io.github.timortel.kmpgrpc.core.Channel

class Greeting {
    private val platform = getPlatform()
    suspend fun greet(): String {
        val channel = Channel.Builder
            .forAddress("grpcb.in", 9000) // replace with your address and your port
            .usePlaintext() // To force grpc to allow plaintext traffic, if you don't call this https is used.
            .build()
        val stub = Hello.HelloServiceStub(channel)
        val request = helloRequest {
                greeting = platform.name
        }
        return try {
            val response  = stub.SayHello(request);
            response.reply
        }catch (e: Exception) {
            "Error $e";
        }
    }
}