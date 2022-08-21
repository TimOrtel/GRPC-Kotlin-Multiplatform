package io.github.timortel.kotlin_multiplatform_grpc_lib.stub

import io.github.timortel.kotlin_multiplatform_grpc_lib.KMChannel
import io.github.timortel.kotlin_multiplatform_grpc_lib.util.TimeUnit

interface IOSKMStub<S : KMStub<S>> {

    val channel: KMChannel

    fun build(channel: KMChannel): S

    fun withDeadlineAfter(duration: Long, unit: TimeUnit): S {
        return build(channel.withDeadlineAfter(duration, unit))
    }
}