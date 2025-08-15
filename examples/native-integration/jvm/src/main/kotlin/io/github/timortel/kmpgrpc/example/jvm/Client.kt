package io.github.timortel.kmpgrpc.example.jvm

import io.github.timortel.kmpgrpc.example.common.GreetingLogic
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        println(GreetingLogic.performGreeting("localhost", 17600, "Greeting from JVM"))
    }
}