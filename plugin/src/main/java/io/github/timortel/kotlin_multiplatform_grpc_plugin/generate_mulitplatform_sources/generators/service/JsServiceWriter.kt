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

object JsServiceWriter : ServiceWriter(true) {

    override val classAndFunctionModifiers: List<KModifier> = listOf(KModifier.ACTUAL)
    override val channelConstructorModifiers: List<KModifier> = listOf(KModifier.ACTUAL)
    override val primaryConstructorModifiers: List<KModifier> = listOf(KModifier.PRIVATE, KModifier.ACTUAL)

    override fun applyToClass(
        builder: TypeSpec.Builder,
        protoFile: ProtoFile,
        service: ProtoService,
        serviceName: ClassName
    ) {
        builder.apply {
            addProperty(
                PropertySpec
                    .builder("channel", kmChannel, KModifier.PRIVATE, KModifier.LATEINIT).mutable(true)
                    .build()
            )
            addProperty(
                PropertySpec
                    .builder(
                        "client",
                        Const.Service.JS.nativeServiceClassName(protoFile, service),
                        KModifier.PRIVATE,
                        KModifier.LATEINIT
                    )
                    .mutable(true)
                    .build()
            )
            addProperty(
                PropertySpec
                    .builder("metadata", kmMetadata, KModifier.OVERRIDE)
                    .mutable(true)
                    .initializer("%T()", kmMetadata)
                    .build()
            )

            addFunction(
                FunSpec
                    .constructorBuilder()
                    .addParameter("channel", kmChannel)
                    .addParameter("metadata", kmMetadata)
                    .callThisConstructor("channel")
                    .addStatement("this.metadata = metadata")
                    .build()
            )

            addFunction(
                FunSpec
                    .builder("build")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("metadata", kmMetadata)
                    .returns(serviceName)
                    .addStatement("return %T(channel, metadata)", serviceName)
                    .build()
            )

            overrideWithDeadlineAfter(builder, serviceName)
        }
    }

    override fun applyToChannelConstructor(
        builder: FunSpec.Builder,
        protoFile: ProtoFile,
        service: ProtoService
    ) {
        builder.apply {
            addStatement("this.%N = %N", "channel", "channel")
            addStatement(
                "this.client = %T(channel.connectionString)",
                Const.Service.JS.nativeServiceClassName(
                    protoFile,
                    service
                )
            )

            callThisConstructor()
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

            addStatement("val actMetadata = this.metadata + metadata")

            val clientCallCode = CodeBlock
                .builder()
                .add("client.${rpc.rpcName}(")
                .add("request.jsImpl, ")
                .add("actMetadata.%M", MemberName("io.github.timortel.kotlin_multiplatform_grpc_lib", "jsMetadata"))
                .apply {
                    if (rpc.isResponseStream) {
                        add(")")
                    } else {
                        add(", callback)")
                    }
                }
                .build()

            addCode("return ")

            if (!rpc.isResponseStream) {
                addCode("%M(", responseCommonMember)
            }

            addCode(
                "%M<%T> {\n",
                if (rpc.isResponseStream)
                    MemberName(
                        "io.github.timortel.kotlin_multiplatform_grpc_lib.rpc", "serverSideStreamingCallImplementation"
                    )
                else MemberName("io.github.timortel.kotlin_multiplatform_grpc_lib.rpc", "simpleCallImplementation"),
                rpc.response.jsType
            )

            if (!rpc.isResponseStream) addCode(" callback -> ")

            addCode(clientCallCode)
            addCode("\n}")

            if (rpc.isResponseStream) {
                addCode(".%M { %M(it) }", MemberName("kotlinx.coroutines.flow", "map"), responseCommonMember)
            } else {
                addCode(")")
            }
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