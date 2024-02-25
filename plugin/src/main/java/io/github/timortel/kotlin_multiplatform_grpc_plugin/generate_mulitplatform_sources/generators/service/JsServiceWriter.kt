package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.service

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoRpc
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoService
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.kmChannel
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.kmMetadata
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.kmStub

object JsServiceWriter : ActualServiceWriter() {

    override val classAndFunctionModifiers: List<KModifier> = listOf(KModifier.ACTUAL)
    override val channelConstructorModifiers: List<KModifier> = listOf(KModifier.ACTUAL)
    override val primaryConstructorModifiers: List<KModifier> = listOf(KModifier.PRIVATE, KModifier.ACTUAL)

    override val callOptionsType: TypeName = kmMetadata
    override val createEmptyCallOptionsCode: CodeBlock = CodeBlock.of("%T()", kmMetadata)

    override fun applyToClass(
        builder: TypeSpec.Builder,
        protoFile: ProtoFile,
        service: ProtoService,
        serviceName: ClassName
    ) {
        super.applyToClass(builder, protoFile, service, serviceName)

        builder.apply {
            addProperty(
                PropertySpec
                    .builder(
                        "client",
                        Const.Service.JS.nativeServiceClassName(protoFile, service),
                        KModifier.PRIVATE,
                    )
                    .initializer(
                        CodeBlock.of(
                            "%T(%N.connectionString)",
                            Const.Service.JS.nativeServiceClassName(protoFile, service),
                            Const.Service.CHANNEL_PROPERTY_NAME
                        )
                    )
                    .build()
            )

            overrideWithDeadlineAfter(builder, serviceName)
        }
    }

    override fun applyToRpcFunction(
        builder: FunSpec.Builder,
        protoFile: ProtoFile,
        service: ProtoService,
        rpc: ProtoRpc
    ) {
        builder.apply {
            val responseCommonMember = Const.Message.CommonFunction.JS.commonFunction(rpc.response.commonType)

            addStatement("val actMetadata = this.%N + metadata", Const.Service.CALL_OPTIONS_PROPERTY_NAME)

            val clientCallCode = CodeBlock
                .builder()
                .add("client.${rpc.rpcName}(")
                .add("request, ")
                .add("actMetadata.%M", MemberName("io.github.timortel.kotlin_multiplatform_grpc_lib", "jsMetadata"))
                .apply {
                    when (rpc.method) {
                        ProtoRpc.Method.UNARY -> add(", callback)")
                        ProtoRpc.Method.SERVER_STREAMING -> add(")")
                    }
                }
                .build()

            addCode("return ")

            addCode(
                "%M {\n",
                when (rpc.method) {
                    ProtoRpc.Method.UNARY -> MemberName(
                        "io.github.timortel.kotlin_multiplatform_grpc_lib.rpc",
                        "simpleCallImplementation"
                    )

                    ProtoRpc.Method.SERVER_STREAMING -> MemberName(
                        "io.github.timortel.kotlin_multiplatform_grpc_lib.rpc", "serverSideStreamingCallImplementation"
                    )
                }
            )

            if (rpc.method == ProtoRpc.Method.UNARY) addCode(" callback -> ")

            addCode(clientCallCode)
            addCode("\n}")
        }
    }

    override fun applyToMetadataParameter(builder: ParameterSpec.Builder, service: ProtoService) = Unit

    override fun specifyInheritance(
        builder: TypeSpec.Builder,
        serviceClass: ClassName,
        protoFile: ProtoFile,
        service: ProtoService
    ) {
        builder.apply {
            superclass(kmStub.parameterizedBy(serviceClass))

            addSuperinterface(
                ClassName("io.github.timortel.kotlin_multiplatform_grpc_lib.stub", "JsStub")
                    .parameterizedBy(serviceClass)
            )
        }
    }
}