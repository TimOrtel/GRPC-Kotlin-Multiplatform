package io.github.timortel.kmpgrpc.core.stub

import io.github.timortel.kmpgrpc.core.CallOptions
import io.github.timortel.kmpgrpc.core.Channel
import kotlin.time.Duration

interface NativeJsStub<S : Stub<S>> {

    val channel: Channel
    val callOptions: CallOptions

    fun build(channel: Channel, callOptions: CallOptions): S

    fun withDeadlineAfter(duration: Duration): S {
        return build(
            channel = channel,
            callOptions = callOptions.copy(deadlineAfter = duration)
        )
    }
}
