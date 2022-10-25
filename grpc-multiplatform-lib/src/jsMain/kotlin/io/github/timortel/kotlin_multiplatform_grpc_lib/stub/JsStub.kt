package io.github.timortel.kotlin_multiplatform_grpc_lib.stub

import io.github.timortel.kotlin_multiplatform_grpc_lib.KMChannel
import io.github.timortel.kotlin_multiplatform_grpc_lib.KMMetadata
import io.github.timortel.kotlin_multiplatform_grpc_lib.util.TimeUnit

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