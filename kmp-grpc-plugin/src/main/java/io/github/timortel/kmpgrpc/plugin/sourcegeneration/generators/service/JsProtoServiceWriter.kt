package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.service

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.*
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.service.ProtoRpc
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.service.ProtoService

object JsProtoServiceWriter : ActualProtoServiceWriter() {

    override val channelConstructorModifiers: List<KModifier> = listOf(KModifier.ACTUAL)
    override val primaryConstructorModifiers: List<KModifier> = listOf(KModifier.PRIVATE, KModifier.ACTUAL)

    override val callOptionsType: TypeName = kmMetadata
    override val createEmptyCallOptionsCode: CodeBlock = CodeBlock.of("%T.empty()", kmMetadata)

    private val unaryCallImpl = MemberName(PACKAGE_RPC, "unaryCallImplementation")
    private val serverStreamingCallImpl = MemberName(PACKAGE_RPC, "serverSideStreamingCallImplementation")

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
        if (rpc.isSendingStream) {
            builder.addCode(
                "throw %T(%S)",
                NotImplementedError::class.asTypeName(),
                "Client side streaming RPCs are not supported for JavaScript targets."
            )

            return
        }

        builder.addCode(
            CodeBlock.builder()
                .apply {
                    val rpcImplMember = if (rpc.isReceivingStream) {
                        serverStreamingCallImpl
                    } else {
                        unaryCallImpl
                    }

                    addStatement(
                        "return %M(channel, %S, request, %T.Companion, this.%N + metadata)",
                        rpcImplMember,
                        "/${rpc.file.`package`}.${rpc.service.name}/${rpc.name}",
                        rpc.returnType.resolve(),
                        Const.Service.CALL_OPTIONS_PROPERTY_NAME
                    )
                }
                .build()
        )
    }

    override fun applyToMetadataParameter(builder: ParameterSpec.Builder, service: ProtoService) = Unit

    override fun specifyInheritance(
        builder: TypeSpec.Builder,
        service: ProtoService
    ) {
        builder.apply {
            superclass(kmStub.parameterizedBy(service.className))

            addSuperinterface(
                ClassName(PACKAGE_STUB, "JsStub")
                    .parameterizedBy(service.className)
            )
        }
    }
}
