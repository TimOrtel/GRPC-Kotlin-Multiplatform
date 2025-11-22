package io.github.timortel.kmpgrpc.core.config

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Represents a configuration for KeepAlive behavior.
 */
sealed interface KeepAliveConfig {

    /**
     * Represents a configuration option where KeepAlive is explicitly disabled.
     *
     * When this configuration is selected, no keep-alive mechanisms will be used.
     */
    data object Disabled : KeepAliveConfig

    /**
     * Represents a configuration where KeepAlive is enabled.
     *
     * @property time The duration that specifies the keep-alive interval.
     * @property timeout The maximum duration to wait before timing out.
     * @property withoutCalls Indicates whether keep-alive should function even without ongoing calls.
     */
    data class Enabled(val time: Duration, val timeout: Duration = 20.seconds, val withoutCalls: Boolean = false) : KeepAliveConfig
}
