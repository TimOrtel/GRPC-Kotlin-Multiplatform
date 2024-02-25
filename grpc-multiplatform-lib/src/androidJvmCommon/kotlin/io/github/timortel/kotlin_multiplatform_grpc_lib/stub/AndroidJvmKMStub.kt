package io.github.timortel.kotlin_multiplatform_grpc_lib.stub

import io.github.timortel.kotlin_multiplatform_grpc_lib.KMChannel
import io.github.timortel.kotlin_multiplatform_grpc_lib.util.TimeUnit
import io.grpc.CallOptions

// Additional layer of abstraction for the second generic argument.
/**
 * The Android and Jvm stub type.
 */
interface AndroidJvmKMStub<S : KMStub<S>> {

    val channel: KMChannel
    val callOptions: CallOptions

    fun build(channel: KMChannel, callOptions: CallOptions): S

    fun withDeadlineAfter(duration: Long, unit: TimeUnit): S {
        return build(channel, callOptions.withDeadlineAfter(duration, unit.javaTimeUnit))
    }
}