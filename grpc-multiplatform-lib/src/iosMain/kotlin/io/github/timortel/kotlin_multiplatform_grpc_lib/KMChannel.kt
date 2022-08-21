package io.github.timortel.kotlin_multiplatform_grpc_lib

import cocoapods.GRPCClient.*

actual class KMChannel(private val name: String, private val port: Int, val callOptions: GRPCCallOptions) {

    fun buildRequestOptions(path: String) = GRPCRequestOptions("$name:$port", path, safety = GRPCCallSafetyDefault)

    actual class Builder(private val name: String, private val port: Int) {

        private val callOptions = GRPCMutableCallOptions()

        actual companion object {
            actual fun forAddress(
                name: String,
                port: Int
            ): Builder = Builder(name, port)
        }

        actual fun usePlaintext(): Builder {
            callOptions.setTransport(GRPCDefaultTransportImplList_.core_insecure)
            return this
        }

        actual fun build(): KMChannel = KMChannel(name, port, callOptions)
    }
}