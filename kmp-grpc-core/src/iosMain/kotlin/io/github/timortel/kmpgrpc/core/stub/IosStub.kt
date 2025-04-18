package io.github.timortel.kmpgrpc.core.stub

import cocoapods.GRPCClient.GRPCCallOptions
import cocoapods.GRPCClient.GRPCMutableCallOptions
import io.github.timortel.kmpgrpc.core.Channel
import io.github.timortel.kmpgrpc.core.Metadata
import io.github.timortel.kmpgrpc.core.util.TimeUnit

/**
 * Ios [Stub] wrapper.
 */
interface IosStub<S : Stub<S>> {

    /**
     * The [Channel] of this stub.
     */
    val channel: Channel

    /**
     * The current [GRPCCallOptions] of this stub.
     */
    val callOptions: GRPCCallOptions

    /**
     * Construct a new channel using both the associated [Channel] and [GRPCCallOptions].
     */
    fun build(channel: Channel, callOptions: GRPCCallOptions): S

    /**
     * @return a new stub that sends the given [metadata] on each request.
     */
    fun withMetadata(metadata: Metadata): S {
        val mutableOptions = callOptions.mutableCopy() as GRPCMutableCallOptions
        mutableOptions.setInitialMetadata(metadata.entries.toMap())

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