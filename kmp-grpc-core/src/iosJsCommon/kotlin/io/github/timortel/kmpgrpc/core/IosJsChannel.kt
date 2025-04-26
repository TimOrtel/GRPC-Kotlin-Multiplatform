package io.github.timortel.kmpgrpc.core

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Abstract base class representing an iOS or JavaScript communication channel.
 *
 * This class provides mechanisms to manage the lifecycle of a channel, including shutdown functionality.
 */
abstract class IosJsChannel {

    internal val isShutdownImmediately = MutableStateFlow(false)

    internal var isShutdown: Boolean = false

    open fun shutdown() {
        isShutdown = true
    }

    fun shutdownNow() {
        shutdown()
        isShutdownImmediately.value = true
    }
}
