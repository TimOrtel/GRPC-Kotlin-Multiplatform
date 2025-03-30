package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.service

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.kmMetadata
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.kmStub
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.service.ProtoRpc
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.service.ProtoService

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
