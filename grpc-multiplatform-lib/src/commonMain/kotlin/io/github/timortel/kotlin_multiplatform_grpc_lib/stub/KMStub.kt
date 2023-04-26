package io.github.timortel.kotlin_multiplatform_grpc_lib.stub

import io.github.timortel.kotlin_multiplatform_grpc_lib.util.TimeUnit

/**
 * A stub allows you to make your rpc requests. Stubs are generated based on your proto definition by the gradle plugin.
 */
abstract class KMStub<S : KMStub<S>> {

    /**
     * @return a new stub that will abort requests after the specified amount of time.
     */
    abstract fun withDeadlineAfter(duration: Long, unit: TimeUnit): S
}