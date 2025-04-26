package io.github.timortel.kmpgrpc.core.stub

import cocoapods.GRPCClient.GRPCCallOptions
import cocoapods.GRPCClient.GRPCMutableCallOptions
import io.github.timortel.kmpgrpc.core.Channel
import io.github.timortel.kmpgrpc.core.Metadata
import kotlin.time.Duration
import kotlin.time.DurationUnit

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
    fun withDeadlineAfter(duration: Duration): S {
        val mutableOptions = callOptions.mutableCopy() as GRPCMutableCallOptions

        mutableOptions.setTimeout(duration.toDouble(DurationUnit.SECONDS))

        return build(channel, mutableOptions)
    }
}