package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.service

import com.squareup.kotlinpoet.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoRpc
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoService
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.iosUnaryCallImplementation
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.kmChannel

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
                .builder(Const.Service.IOS.CHANNEL_PROPERTY_NAME, kmChannel, KModifier.PRIVATE)
                .build()
        )
    }

    override fun applyToChannelConstructor(builder: FunSpec.Builder, protoFile: ProtoFile, service: ProtoService) {
        builder.addStatement(
            "this.%N = %N",
            Const.Service.IOS.CHANNEL_PROPERTY_NAME,
            Const.Service.Constructor.CHANNEL_PARAMETER_NAME
        )
    }

    override fun applyToRpcFunction(
        builder: FunSpec.Builder,
        protoFile: ProtoFile,
        service: ProtoService,
        rpc: ProtoRpc
    ) {
        if (rpc.isResponseStream) {

        } else {
            //unary rpc, simply call the implementation
            builder.addStatement(
                "return %M(%N, %S, %N, %T.Companion)",
                iosUnaryCallImplementation,
                Const.Service.IOS.CHANNEL_PROPERTY_NAME,
                "/${protoFile.pkg}/${rpc.rpcName}",
                Const.Service.RpcCall.PARAM_REQUEST,
                rpc.response.iosType
            )
        }
    }

    override fun applyToMetadataParameter(builder: ParameterSpec.Builder, service: ProtoService) {

    }

    override fun specifyInheritance(
        builder: TypeSpec.Builder,
        serviceClass: ClassName,
        protoFile: ProtoFile,
        service: ProtoService
    ) {

    }
}