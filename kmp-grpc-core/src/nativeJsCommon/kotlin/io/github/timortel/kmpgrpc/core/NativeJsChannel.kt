package io.github.timortel.kmpgrpc.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Abstract base class representing an iOS or JavaScript communication channel.
 *
 * This class provides mechanisms to manage the lifecycle of a channel, including shutdown functionality.
 */
abstract class NativeJsChannel {

    private val cleanupMutex = Mutex()

    internal val isShutdownImmediately = MutableStateFlow(false)

    internal var isShutdown: Boolean = false

    private val activeRpcs = MutableStateFlow(0)

    internal var hasCleanedUpResources = false

    val isTerminated: Boolean
        get() = isShutdown && activeRpcs.value == 0 && hasCleanedUpResources

    open suspend fun shutdown() {
        isShutdown = true

        cleanupMutex.withLock {
            activeRpcs.first { it == 0 }

            if (hasCleanedUpResources) return@withLock

            cleanupResources()

            hasCleanedUpResources = true
        }
    }

    suspend fun shutdownNow() {
        isShutdownImmediately.value = true

        shutdown()
    }

    abstract fun cleanupResources()

    internal fun registerRpc() {
        activeRpcs.update { it + 1 }
    }

    internal fun unregisterRpc() {
        activeRpcs.update { it - 1 }
    }
}
