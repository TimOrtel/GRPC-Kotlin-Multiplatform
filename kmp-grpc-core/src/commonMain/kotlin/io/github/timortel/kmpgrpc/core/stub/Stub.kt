package io.github.timortel.kmpgrpc.core.stub

import kotlin.time.Duration

/**
 * A stub allows you to make your rpc requests. Stubs are generated based on your proto definition by the gradle plugin.
 */
abstract class Stub<S : Stub<S>> {

    /**
     * @return a new stub that will abort requests after the specified amount of time.
     */
    abstract fun withDeadlineAfter(duration: Duration): S
}
