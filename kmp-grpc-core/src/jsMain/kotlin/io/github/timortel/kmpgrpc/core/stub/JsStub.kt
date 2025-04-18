package io.github.timortel.kmpgrpc.core.stub

import io.github.timortel.kmpgrpc.core.Channel
import io.github.timortel.kmpgrpc.core.Metadata
import io.github.timortel.kmpgrpc.core.util.TimeUnit

interface JsStub<S : KMStub<S>> {

    val channel: Channel
    val callOptions: Metadata

    fun build(channel: Channel, callOptions: Metadata): S

    fun withDeadlineAfter(duration: Long, unit: TimeUnit): S {
        return build(
            channel = channel,
            callOptions = callOptions.withEntry("deadline", (duration * unit.toMilliFactor).toString())
        )
    }
}
