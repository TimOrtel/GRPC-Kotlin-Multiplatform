package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.service

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.kmTimeUnit

fun overrideWithDeadlineAfter(builder: TypeSpec.Builder, serviceClass: ClassName) {
    builder.addFunction(
        FunSpec.builder("withDeadlineAfter")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("duration", Long::class)
            .addParameter("unit", kmTimeUnit)
            .returns(serviceClass)
            .addStatement("return super.withDeadlineAfter(duration, unit)")
            .build()
    )
}