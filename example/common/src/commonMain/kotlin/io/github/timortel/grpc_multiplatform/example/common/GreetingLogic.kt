package io.github.timortel.grpc_multiplatform.example.common

import io.github.timortel.kotlin_multiplatform_grpc_lib.KMChannel
import io.github.timortel.kotlin_multiplatform_grpc_lib.util.TimeUnit
import io.github.timortel.grpc_multiplatform.example.KMHelloServiceStub
import io.github.timortel.grpc_multiplatform.example.kmHelloRequest

object GreetingLogic {

    suspend fun performGreeting(host: String, port: Int, message: String): String {
        val channel = KMChannel.Builder
            .forAddress(host, port)
            .usePlaintext()
            .build()

        val stub = KMHelloServiceStub(channel)
        return try {
            stub
                .withDeadlineAfter(3, TimeUnit.SECONDS)
                .sayHello(
                request = kmHelloRequest {
                    greeting = message
                }
            ).response
        } catch (e: Exception) {
            e.printStackTrace()
            e.message ?: "An error occurred"
        }
    }

    suspend fun performMultipleGreetings(message: String, port: Int) {
        val channel = KMChannel.Builder
            .forAddress("localhost", port)
            .usePlaintext()
            .build()

        val stub = KMHelloServiceStub(channel)

        val request = kmHelloRequest {
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
