package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators

import com.squareup.kotlinpoet.AnnotationSpec

object DefaultAnnotations {
    val Deprecated = AnnotationSpec.builder(kotlin.Deprecated::class)
        .addMember("%S", "Set deprecated by protobuf option")
        .build()
}
