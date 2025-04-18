package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.service

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.*
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.service.ProtoRpc
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.service.ProtoService

object IosProtoServiceWriter : ActualProtoServiceWriter() {

    override val channelConstructorModifiers: List<KModifier> = listOf(KModifier.ACTUAL)
    override val primaryConstructorModifiers: List<KModifier> = listOf(KModifier.PRIVATE, KModifier.ACTUAL)

    private val GRPC_MUTABLE_CALL_OPTIONS = ClassName("cocoapods.GRPCClient", "GRPCMutableCallOptions")

    override val callOptionsType: TypeName = ClassName("cocoapods.GRPCClient", "GRPCCallOptions")
    override val createEmptyCallOptionsCode: CodeBlock =
        CodeBlock.of("%T()", GRPC_MUTABLE_CALL_OPTIONS)

    private val iosStub = ClassName(PACKAGE_STUB, "IosStub")

    override fun applyToClass(
        builder: TypeSpec.Builder,
        service: ProtoService
    ) {
        super.applyToClass(builder, service)

        overrideWithDeadlineAfter(builder, service.className)
    }

    override fun applyToRpcFunction(
        builder: FunSpec.Builder,
        rpc: ProtoRpc
    ) {
        val impl = if (rpc.isReceivingStream) {
            iosServerSideStreamingCallImplementation
        } else {
            iosUnaryCallImplementation
        }

        builder.apply {
            addStatement(
                "val callOptions = %N.mutableCopy() as %T",
                Const.Service.CALL_OPTIONS_PROPERTY_NAME,
                GRPC_MUTABLE_CALL_OPTIONS
            )
            addStatement("callOptions.setInitialMetadata(%N.entries.toMap())", Const.Service.RpcCall.PARAM_METADATA)
        }

        builder.addStatement(
            "return %M(%N, callOptions, %S, %N, %T.Companion, %T.Companion)",
            impl,
            Const.Service.CHANNEL_PROPERTY_NAME,
            "/${rpc.file.`package`.orEmpty()}.${rpc.service.name}/${rpc.name}",
            Const.Service.RpcCall.PARAM_REQUEST,
            rpc.sendType.resolve(),
            rpc.returnType.resolve()
        )
    }

    override fun applyToMetadataParameter(builder: ParameterSpec.Builder, service: ProtoService) {
    }

    override fun specifyInheritance(
        builder: TypeSpec.Builder,
        service: ProtoService
    ) {
        builder.superclass(kmStub.parameterizedBy(service.className))
        builder.addSuperinterface(iosStub.parameterizedBy(service.className))
    }
}