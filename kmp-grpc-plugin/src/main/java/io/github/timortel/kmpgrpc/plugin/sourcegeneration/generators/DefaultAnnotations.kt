package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import io.github.timortel.kmpgrpc.shared.internal.InternalKmpGrpcApi

object DefaultAnnotations {
    val Deprecated = AnnotationSpec.builder(kotlin.Deprecated::class)
        .addMember("%S", "Set deprecated by protobuf option")
        .build()

    val SuppressDeprecation = AnnotationSpec.builder(Suppress::class)
        .addMember("%S", "DEPRECATION")
        .build()

    val OptIntoKmpGrpcInternalApi = AnnotationSpec.builder(ClassName("kotlin", "OptIn"))
        .addMember("%T::class", InternalKmpGrpcApi::class)
        .build()
}
