package io.github.timortel.kmpgrpc.core.stub

import io.github.timortel.kmpgrpc.core.KMChannel
import io.github.timortel.kmpgrpc.core.Metadata
import io.github.timortel.kmpgrpc.core.util.TimeUnit

interface JsStub<S : KMStub<S>> {

    val channel: KMChannel
    val callOptions: Metadata

    fun build(channel: KMChannel, callOptions: Metadata): S

    fun withDeadlineAfter(duration: Long, unit: TimeUnit): S {
        return build(
            channel = channel,
            callOptions = callOptions.withEntry("deadline", (duration * unit.toMilliFactor).toString())
        )
    }
}
