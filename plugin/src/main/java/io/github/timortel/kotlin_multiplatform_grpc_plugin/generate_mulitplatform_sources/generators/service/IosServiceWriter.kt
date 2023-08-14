package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.service

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoRpc
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoService
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const

object IosServiceWriter : ActualServiceWriter() {

    override val classAndFunctionModifiers: List<KModifier> = listOf(KModifier.ACTUAL)
    override val channelConstructorModifiers: List<KModifier> = listOf(KModifier.ACTUAL)
    override val primaryConstructorModifiers: List<KModifier> = listOf(KModifier.PRIVATE, KModifier.ACTUAL)

    private val GRPC_MUTABLE_CALL_OPTIONS = ClassName("cocoapods.GRPCClient", "GRPCMutableCallOptions")

    override val callOptionsType: TypeName = ClassName("cocoapods.GRPCClient", "GRPCCallOptions")
    override val createEmptyCallOptionsCode: CodeBlock =
        CodeBlock.of("%T()", GRPC_MUTABLE_CALL_OPTIONS)

    override fun applyToClass(
        builder: TypeSpec.Builder,
        protoFile: ProtoFile,
        service: ProtoService,
        serviceName: ClassName
    ) {
        super.applyToClass(builder, protoFile, service, serviceName)

        overrideWithDeadlineAfter(builder, serviceName)
    }

    override fun applyToRpcFunction(
        builder: FunSpec.Builder,
        protoFile: ProtoFile,
        service: ProtoService,
        rpc: ProtoRpc
    ) {
        val impl = when (rpc.method) {
            ProtoRpc.Method.UNARY -> iosUnaryCallImplementation
            ProtoRpc.Method.SERVER_STREAMING -> iosServerSideStreamingCallImplementation
        }

        builder.apply {
            addStatement(
                "val callOptions = %N.mutableCopy() as %T",
                Const.Service.CALL_OPTIONS_PROPERTY_NAME,
                GRPC_MUTABLE_CALL_OPTIONS
            )
            addStatement("callOptions.setInitialMetadata(%N.metadataMap.toMap())", Const.Service.RpcCall.PARAM_METADATA)
        }

        builder.addStatement(
            "return %M(%N, callOptions, %S, %N, %T.Companion)",
            impl,
            Const.Service.CHANNEL_PROPERTY_NAME,
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