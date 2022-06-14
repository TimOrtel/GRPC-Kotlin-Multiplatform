package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.service

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kotlin_multiplatform_grpc_lib.KMMetadata
import io.github.timortel.kotlin_multiplatform_grpc_lib.stub.KMStub
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoRpc
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoService

object CommonServiceWriter : ServiceWriter(false) {

    override val classAndFunctionModifiers: List<KModifier> = listOf(KModifier.EXPECT)
    override val channelConstructorModifiers: List<KModifier> = emptyList()
    override val primaryConstructorModifiers: List<KModifier> = listOf(KModifier.PRIVATE)

    override fun applyToClass(builder: TypeSpec.Builder, protoFile: ProtoFile, service: ProtoService, serviceName: ClassName) = Unit

    override fun applyToRpcFunction(builder: FunSpec.Builder, service: ProtoService, rpc: ProtoRpc) = Unit

    override fun applyToMetadataParameter(builder: ParameterSpec.Builder, service: ProtoService) {
        builder.defaultValue("%T()", KMMetadata::class)
    }

    override fun applyToChannelConstructor(builder: FunSpec.Builder, protoFile: ProtoFile, service: ProtoService) = Unit

    override fun specifyInheritance(
        builder: TypeSpec.Builder,
        serviceClass: ClassName,
        protoFile: ProtoFile,
        service: ProtoService
    ) {
        builder.superclass(KMStub::class.asClassName().parameterizedBy(serviceClass))
    }
}