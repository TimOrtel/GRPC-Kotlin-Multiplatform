package io.github.timortel.kotlin_multiplatform_grpc_lib

import cocoapods.GRPCClient.*
import io.github.timortel.kotlin_multiplatform_grpc_lib.util.TimeUnit

actual class KMChannel(private val name: String, private val port: Int, val callOptions: GRPCCallOptions) {

    fun buildRequestOptions(path: String) = GRPCRequestOptions("$name:$port", path, safety = GRPCCallSafetyDefault)

    fun withDeadlineAfter(duration: Long, unit: TimeUnit): KMChannel {
        val mutableOptions = callOptions.mutableCopy() as GRPCMutableCallOptions

        val millis = (duration * unit.toMilliFactor).toDouble()
        val seconds = millis / 1000.0

        mutableOptions.setTimeout(seconds)

        return KMChannel(name, port, mutableOptions)
    }

    fun withMetadata(metadata: KMMetadata): KMChannel {
        val mutableOptions = callOptions.mutableCopy() as GRPCMutableCallOptions
        mutableOptions.setInitialMetadata(metadata.metadataMap.toMap())

        return KMChannel(name, port, mutableOptions)
    }

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