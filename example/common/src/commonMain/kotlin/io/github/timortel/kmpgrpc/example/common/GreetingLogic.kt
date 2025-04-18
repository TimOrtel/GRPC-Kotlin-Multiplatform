package io.github.timortel.kmpgrpc.example.common

import io.github.timortel.kmpgrpc.example.HelloServiceStub
import io.github.timortel.kmpgrpc.example.helloRequest
import io.github.timortel.kmpgrpc.core.Channel
import io.github.timortel.kmpgrpc.core.util.TimeUnit

object GreetingLogic {

    suspend fun performGreeting(host: String, port: Int, message: String): String {
        val channel = Channel.Builder
            .forAddress(host, port)
            .usePlaintext()
            .build()

        val stub = HelloServiceStub(channel)
        return try {
            stub
                .withDeadlineAfter(3, TimeUnit.SECONDS)
                .sayHello(
                request = helloRequest {
                    greeting = message
                }
            ).response
        } catch (e: Exception) {
            e.printStackTrace()
            e.message ?: "An error occurred"
        }
    }

    suspend fun performMultipleGreetings(message: String, port: Int) {
        val channel = Channel.Builder
            .forAddress("localhost", port)
            .usePlaintext()
            .build()

        val stub = HelloServiceStub(channel)

        val request = helloRequest {
            greeting = message
        }

        stub
            .withDeadlineAfter(10, TimeUnit.SECONDS)
            .sayHelloMultipleTimes(request)
            .collect {
                println("Received: ${it.response}")
            }
    }
}
