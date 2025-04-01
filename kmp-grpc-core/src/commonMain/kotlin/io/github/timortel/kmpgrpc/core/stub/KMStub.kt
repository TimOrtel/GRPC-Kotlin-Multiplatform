package io.github.timortel.kmpgrpc.core.stub

import io.github.timortel.kmpgrpc.core.util.TimeUnit

/**
 * A stub allows you to make your rpc requests. Stubs are generated based on your proto definition by the gradle plugin.
 */
abstract class KMStub<S : KMStub<S>> {

    /**
     * @return a new stub that will abort requests after the specified amount of time.
     */
    abstract fun withDeadlineAfter(duration: Long, unit: TimeUnit): S
}