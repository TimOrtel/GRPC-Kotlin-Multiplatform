package io.github.timortel.kotlin_multiplatform_grpc_lib.stub

import io.github.timortel.kotlin_multiplatform_grpc_lib.util.TimeUnit
import io.grpc.stub.AbstractStub

//Additional layer of abstraction for the second generic argument.
interface AndroidJvmKMStub<S : KMStub<S>, NATIVE_STUB : AbstractStub<NATIVE_STUB>> {

    val impl: NATIVE_STUB

    fun build(impl: NATIVE_STUB): S

    fun withDeadlineAfter(duration: Long, unit: TimeUnit): S {
        return build(impl.withDeadlineAfter(duration, unit.javaTimeUnit))
    }
}