package io.github.timortel.kotlin_multiplatform_grpc_lib.stub

import cocoapods.GRPCClient.GRPCCallOptions
import cocoapods.GRPCClient.GRPCMutableCallOptions
import io.github.timortel.kotlin_multiplatform_grpc_lib.KMChannel
import io.github.timortel.kotlin_multiplatform_grpc_lib.KMMetadata
import io.github.timortel.kotlin_multiplatform_grpc_lib.util.TimeUnit

/**
 * Ios [KMStub] wrapper.
 */
interface IOSKMStub<S : KMStub<S>> {

    /**
     * The [KMChannel] of this stub.
     */
    val channel: KMChannel

    /**
     * The current [GRPCCallOptions] of this stub.
     */
    val callOptions: GRPCCallOptions

    /**
     * Construct a new channel using both the associated [KMChannel] and [GRPCCallOptions].
     */
    fun build(channel: KMChannel, callOptions: GRPCCallOptions): S

    /**
     * @return a new stub that sends the given [metadata] on each request.
     */
    fun withMetadata(metadata: KMMetadata): S {
        val mutableOptions = callOptions.mutableCopy() as GRPCMutableCallOptions
        mutableOptions.setInitialMetadata(metadata.metadataMap.toMap())

        return build(channel, mutableOptions)
    }

    /**
     * @return a new stub that aborts every call after the specified deadline.
     */
    fun withDeadlineAfter(duration: Long, unit: TimeUnit): S {
        val mutableOptions = callOptions.mutableCopy() as GRPCMutableCallOptions

        val millis = (duration * unit.toMilliFactor).toDouble()
        val seconds = millis / 1000.0

        mutableOptions.setTimeout(seconds)

        return build(channel, mutableOptions)
    }
}