package io.github.timortel.kmpgrpc.core.stub

import io.github.timortel.kmpgrpc.core.KMChannel
import io.github.timortel.kmpgrpc.core.KMMetadata
import io.github.timortel.kmpgrpc.core.util.TimeUnit

interface JsStub<S : KMStub<S>> {

    val channel: KMChannel
    val callOptions: KMMetadata

    fun build(channel: KMChannel, callOptions: KMMetadata): S

    fun withDeadlineAfter(duration: Long, unit: TimeUnit): S {
        val newMetadata = callOptions.copy()
        newMetadata["deadline"] = (duration * unit.toMilliFactor).toString()

        return build(channel, newMetadata)
    }
}