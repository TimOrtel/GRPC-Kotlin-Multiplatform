package io.github.timortel.kotlin_multiplatform_grpc_plugin

import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.Warnings
import io.mockk.MockKMatcherScope

fun MockKMatcherScope.matchWarning(warning: Warnings.Warning) = match<String> { string ->
    warning.isWarning(string)
}
