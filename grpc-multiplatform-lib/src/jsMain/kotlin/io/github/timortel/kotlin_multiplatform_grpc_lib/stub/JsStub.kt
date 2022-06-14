package io.github.timortel.kotlin_multiplatform_grpc_lib.stub

import io.github.timortel.kotlin_multiplatform_grpc_lib.KMMetadata
import io.github.timortel.kotlin_multiplatform_grpc_lib.util.TimeUnit

interface JsStub<S : KMStub<S>> {

    val metadata: KMMetadata

    fun build(metadata: KMMetadata): S

    fun withDeadlineAfter(duration: Long, unit: TimeUnit): S {
        val newMetadata = metadata.copy()
        newMetadata["deadline"] = (duration * unit.toMilliFactor).toString()

        return build(newMetadata)
    }
}