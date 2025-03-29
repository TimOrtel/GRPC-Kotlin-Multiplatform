package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.service

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.constants.Const

fun overrideWithDeadlineAfter(builder: TypeSpec.Builder, serviceClass: ClassName) {
    builder.addFunction(
        FunSpec.builder(Const.Service.Functions.WithDeadlineAfter.NAME)
            .addModifiers(KModifier.OVERRIDE, KModifier.ACTUAL)
            .addParameter(Const.Service.Functions.WithDeadlineAfter.ParamDuration.toParamSpec())
            .addParameter(Const.Service.Functions.WithDeadlineAfter.ParamUnit.toParamSpec())
            .returns(serviceClass)
            .addStatement(
                "return super.%N(%N, %N)",
                Const.Service.Functions.WithDeadlineAfter.NAME,
                Const.Service.Functions.WithDeadlineAfter.ParamDuration.toParamSpec(),
                Const.Service.Functions.WithDeadlineAfter.ParamUnit.toParamSpec()
            )
            .build()
    )
}