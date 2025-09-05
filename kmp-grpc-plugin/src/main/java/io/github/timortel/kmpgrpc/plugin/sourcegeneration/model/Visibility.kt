package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model

import com.squareup.kotlinpoet.KModifier

enum class Visibility(val modifier: KModifier) {
    PUBLIC(KModifier.PUBLIC),
    INTERNAL(KModifier.INTERNAL)
}
