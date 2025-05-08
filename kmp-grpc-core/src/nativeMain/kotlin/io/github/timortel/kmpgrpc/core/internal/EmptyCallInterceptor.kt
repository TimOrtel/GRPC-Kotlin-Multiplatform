package io.github.timortel.kmpgrpc.core.internal

import io.github.timortel.kmpgrpc.core.CallInterceptor

/**
 * Implementation that just returns the arguments.
 */
internal object EmptyCallInterceptor : CallInterceptor
