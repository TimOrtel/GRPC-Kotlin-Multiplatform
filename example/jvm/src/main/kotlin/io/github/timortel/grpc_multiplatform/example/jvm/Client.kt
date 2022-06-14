package io.github.timortel.grpc_multiplatform.jvm;

import io.github.timortel.grpc_multiplatform.example.common.performGreeting
import io.github.timortel.grpc_multiplatform.example.common.performMultipleGreetings
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        println(performGreeting("Greeting from JVM", 17600))

        performMultipleGreetings("Multiple greetings from JVM", 17600)
    }
}