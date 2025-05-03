package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.service

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.*
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.service.ProtoRpc
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.service.ProtoService

object IosProtoServiceWriter : IosJvmProtoServiceWriter() {

    override val channelConstructorModifiers: List<KModifier> = listOf(KModifier.ACTUAL)
    override val primaryConstructorModifiers: List<KModifier> = listOf(KModifier.PRIVATE, KModifier.ACTUAL)

    override val callOptionsType: TypeName = iosJsCallOptions
    override val createEmptyCallOptionsCode: CodeBlock = CodeBlock.of("%T()", callOptionsType)

    override val unaryCallMemberName: MemberName = MemberName(PACKAGE_RPC, "unaryCallImplementation")
    override val clientStreamingCallMemberName: MemberName = MemberName(PACKAGE_RPC, "clientStreamingCallImplementation")
    override val serverStreamingCallMemberName: MemberName = MemberName(PACKAGE_RPC, "serverSideStreamingCallImplementation")
    override val bidiStreamingCallMemberName: MemberName = MemberName(PACKAGE_RPC, "bidiStreamingCallImplementation")

    override fun applyToRpcFunction(
        builder: FunSpec.Builder,
        rpc: ProtoRpc,
        rpcImplementation: MemberName,
        requestParamName: String
    ) {
        builder.addStatement(
            "return %M(%N, %N + %N, %S, %N, %T.Companion)",
            rpcImplementation,
            Const.Service.CHANNEL_PROPERTY_NAME,
            Const.Service.CALL_OPTIONS_PROPERTY_NAME,
            Const.Service.RpcCall.PARAM_METADATA,
            "/${rpc.file.`package`.orEmpty()}.${rpc.service.name}/${rpc.name}",
            requestParamName,
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
        builder.addSuperinterface(iosJsStub.parameterizedBy(service.className))
    }
}
