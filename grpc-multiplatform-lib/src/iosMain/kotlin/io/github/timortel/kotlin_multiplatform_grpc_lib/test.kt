package io.github.timortel.kotlin_multiplatform_grpc_lib

import cocoapods.GRPCClient.GRPCCall
import cocoapods.Protobuf.GPBMessage

fun main() {
    GPBMessage
}

class TestMessage : GPBMessage() {
    var foo: String = ""
}