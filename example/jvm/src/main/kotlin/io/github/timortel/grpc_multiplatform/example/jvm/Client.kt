package io.github.timortel.grpc_multiplatform.example.jvm

import io.github.timortel.grpc_multiplatform.example.common.GreetingLogic
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        println(GreetingLogic.performGreeting("localhost", 17600, "Greeting from JVM"))

        GreetingLogic.performMultipleGreetings("Multiple greetings from JVM", 17600)
    }
}