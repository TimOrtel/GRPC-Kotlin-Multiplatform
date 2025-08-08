package io.github.timortel.kmpgrpc.example.common

import hello.HelloServiceStub
import hello.helloRequest
import io.github.timortel.kmpgrpc.core.Channel
import kotlin.time.Duration.Companion.seconds

object GreetingLogic {

    suspend fun performGreeting(host: String, port: Int, useHttps: Boolean, message: String): String {
        val channel = Channel.Builder
            .forAddress(host, port)
            .let { if (useHttps) it else it.usePlaintext() }
            .build()

        val stub = HelloServiceStub(channel)
        return try {
            stub
                .withDeadlineAfter(3.seconds)
                .SayHello(
                    request = helloRequest {
                        greeting = message
                    }
                ).response
        } catch (e: Exception) {
            e.printStackTrace()
            e.message ?: "An error occurred"
        }
    }
}
