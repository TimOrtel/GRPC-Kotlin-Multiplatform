package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.service

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.kmMetadata
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.kmStub
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.service.ProtoRpc
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.service.ProtoService

object CommonProtoServiceWriter : ProtoServiceWriter(false) {

    override val channelConstructorModifiers: List<KModifier> = emptyList()
    override val primaryConstructorModifiers: List<KModifier> = listOf(KModifier.PRIVATE)

    override fun applyToClass(
        builder: TypeSpec.Builder,
        service: ProtoService
    ) {
        builder.addFunction(
            FunSpec
                .builder(Const.Service.Functions.WithDeadlineAfter.NAME)
                .addParameter(Const.Service.Functions.WithDeadlineAfter.ParamDuration.toParamSpec())
                .addParameter(Const.Service.Functions.WithDeadlineAfter.ParamUnit.toParamSpec())
                .addModifiers(KModifier.OVERRIDE)
                .returns(service.className)
                .build()
        )
    }

    override fun applyToRpcFunction(builder: FunSpec.Builder, rpc: ProtoRpc) = Unit

    override fun applyToMetadataParameter(builder: ParameterSpec.Builder, service: ProtoService) {
        builder.defaultValue("%T()", kmMetadata)
    }

    override fun applyToChannelConstructor(builder: FunSpec.Builder, service: ProtoService) = Unit

    override fun specifyInheritance(builder: TypeSpec.Builder, service: ProtoService) {
        builder.superclass(kmStub.parameterizedBy(service.className))
    }
}
