package io.github.timortel.kmpgrpc.core.stub

import io.github.timortel.kmpgrpc.core.CallOptions
import io.github.timortel.kmpgrpc.core.Channel
import io.github.timortel.kmpgrpc.core.Metadata
import kotlin.time.Duration

/**
 * Ios [Stub] wrapper.
 */
interface IosStub<S : Stub<S>> {

    /**
     * The [Channel] of this stub.
     */
    val channel: Channel
    val callOptions: CallOptions

    fun build(channel: Channel, callOptions: CallOptions): S

    /**
     * @return a new stub that sends the given [metadata] on each request.
     */
    fun withMetadata(metadata: Metadata): S {
        return build(channel, callOptions = CallOptions())
    }

    /**
     * @return a new stub that aborts every call after the specified deadline.
     */
    fun withDeadlineAfter(duration: Duration): S {
        return build(channel, CallOptions())
    }
}