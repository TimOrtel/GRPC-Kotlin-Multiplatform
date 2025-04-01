package io.github.timortel.kmpgrpc.testserver

fun main() {
    TestServer.start().awaitTermination()
}