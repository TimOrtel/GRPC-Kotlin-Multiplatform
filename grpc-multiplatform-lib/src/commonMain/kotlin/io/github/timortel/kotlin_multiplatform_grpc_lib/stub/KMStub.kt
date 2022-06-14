package io.github.timortel.kotlin_multiplatform_grpc_lib.stub

import io.github.timortel.kotlin_multiplatform_grpc_lib.util.TimeUnit

abstract class KMStub<S : KMStub<S>> {
    abstract fun withDeadlineAfter(duration: Long, unit: TimeUnit): S
}