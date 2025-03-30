package io.github.timortel.kmpgrpc.core

import cocoapods.GRPCClient.*

/**
 * On ios the channel equivalent are the [GRPCCallOptions].
 */
actual class KMChannel(private val name: String, private val port: Int, private val usePlaintext: Boolean) {

    fun buildRequestOptions(path: String) = GRPCRequestOptions("$name:$port", path, safety = GRPCCallSafetyDefault)

    /**
     * Applies configuration of the channel to the given call options.
     * If any mutations are performed, a new copy of call options is returned. The original call options
     * are left unmodified.
     */
    fun applyToCallOptions(callOptions: GRPCCallOptions): GRPCCallOptions {
        return if (usePlaintext) {
            val newCallOptions = callOptions.mutableCopy() as GRPCMutableCallOptions
            newCallOptions.setTransport(GRPCDefaultTransportImplList_.core_insecure)
            newCallOptions
        } else callOptions
    }

    actual class Builder(private val name: String, private val port: Int) {

        private var usePlaintext = false

        actual companion object {
            actual fun forAddress(
                name: String,
                port: Int
            ): Builder = Builder(name, port)
        }

        actual fun usePlaintext(): Builder {
            usePlaintext = true
            return this
        }

        actual fun build(): KMChannel = KMChannel(name, port, usePlaintext)
    }
}