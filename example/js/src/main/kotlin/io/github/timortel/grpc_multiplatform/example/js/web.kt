package io.github.timortel.grpc_multiplatform.example.js

import io.github.timortel.grpc_multiplatform.example.common.performGreeting
import io.github.timortel.grpc_multiplatform.example.common.performMultipleGreetings

suspend fun main() {
    val response = performGreeting("Hello from web", 8082)

    console.log(response)

    performMultipleGreetings("Multiple greetings from web", 8082)
}