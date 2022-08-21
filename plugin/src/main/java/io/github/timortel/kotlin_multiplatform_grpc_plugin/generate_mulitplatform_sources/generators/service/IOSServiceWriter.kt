package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.service

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoRpc
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoService
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const

object IOSServiceWriter : ServiceWriter(true) {

    override val classAndFunctionModifiers: List<KModifier> = listOf(KModifier.ACTUAL)
    override val channelConstructorModifiers: List<KModifier> = listOf(KModifier.ACTUAL)
    override val primaryConstructorModifiers: List<KModifier> = listOf(KModifier.PRIVATE, KModifier.ACTUAL)

    override fun applyToClass(
        builder: TypeSpec.Builder,
        protoFile: ProtoFile,
        service: ProtoService,
        serviceName: ClassName
    ) {
        builder.addProperty(
            PropertySpec
                .builder(Const.Service.IOS.CHANNEL_PROPERTY_NAME, kmChannel, KModifier.OVERRIDE, KModifier.LATEINIT)
                .mutable()
                .build()
        )

        builder.addFunction(
            FunSpec
                .builder("build")
                .addModifiers(KModifier.OVERRIDE)
                .returns(serviceName)
                .addParameter("channel", kmChannel)
                .addStatement("return %T(channel)", serviceName)
                .build()
        )

        overrideWithDeadlineAfter(builder, serviceName)
    }

    override fun applyToChannelConstructor(builder: FunSpec.Builder, protoFile: ProtoFile, service: ProtoService) {
        builder.addStatement(
            "this.%N = %N",
            Const.Service.IOS.CHANNEL_PROPERTY_NAME,
            Const.Service.Constructor.CHANNEL_PARAMETER_NAME
        )

        builder.callThisConstructor()
    }

    override fun applyToRpcFunction(
        builder: FunSpec.Builder,
        protoFile: ProtoFile,
        service: ProtoService,
        rpc: ProtoRpc
    ) {
        val impl = if (rpc.isResponseStream) iosServerSideStreamingCallImplementation else iosUnaryCallImplementation

        builder.addStatement(
            "return %M(%N.withMetadata(%N), %S, %N, %T.Companion)",
            impl,
            Const.Service.IOS.CHANNEL_PROPERTY_NAME,
            Const.Service.RpcCall.PARAM_METADATA,
            "/${protoFile.pkg}.${service.serviceName}/${rpc.rpcName}",
            Const.Service.RpcCall.PARAM_REQUEST,
            rpc.response.iosType
        )
    }

    override fun applyToMetadataParameter(builder: ParameterSpec.Builder, service: ProtoService) {
    }

    override fun specifyInheritance(
        builder: TypeSpec.Builder,
        serviceClass: ClassName,
        protoFile: ProtoFile,
        service: ProtoService
    ) {
        builder.superclass(kmStub.parameterizedBy(serviceClass))
        builder.addSuperinterface(
            ClassName(
                "io.github.timortel.kotlin_multiplatform_grpc_lib.stub",
                "IOSKMStub"
            ).parameterizedBy(serviceClass)
        )
    }
}