package io.github.timortel.kmpgrpc.core.stub

import io.github.timortel.kmpgrpc.core.Channel
import io.grpc.CallOptions
import kotlin.time.Duration
import kotlin.time.toJavaDuration

/**
 * The Android and Jvm stub type.
 */
interface AndroidJvmStub<S : Stub<S>> {

    val channel: Channel
    val callOptions: CallOptions

    fun build(channel: Channel, callOptions: CallOptions): S

    fun withDeadlineAfter(duration: Duration): S {
        return build(channel, callOptions.withDeadlineAfter(duration.toJavaDuration()))
    }
}
