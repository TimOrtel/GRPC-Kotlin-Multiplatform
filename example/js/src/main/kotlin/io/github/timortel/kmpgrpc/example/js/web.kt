package io.github.timortel.kmpgrpc.example.js

import io.github.timortel.kmpgrpc.example.common.GreetingLogic

suspend fun main() {
    val response = GreetingLogic.performGreeting("localhost", 8082, "Hello from web")

    console.log(response)

    GreetingLogic.performMultipleGreetings("Multiple greetings from web", 8082)
}