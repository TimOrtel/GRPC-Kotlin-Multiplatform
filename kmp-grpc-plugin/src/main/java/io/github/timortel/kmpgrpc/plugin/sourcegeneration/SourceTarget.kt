package io.github.timortel.kmpgrpc.plugin.sourcegeneration

sealed interface SourceTarget {

    data object Common : SourceTarget
    data object Jvm : SourceTarget, Actual
    data object Js : SourceTarget, Actual
    data object Ios : SourceTarget, Actual

    sealed interface Actual : SourceTarget
}
