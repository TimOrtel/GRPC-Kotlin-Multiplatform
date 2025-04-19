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

    private val grpcWebClientBase =
        ClassName(PACKAGE_EXTERNAL, "GrpcWebClientBase")
    private val methodDescriptor =
        ClassName(PACKAGE_EXTERNAL, "MethodDescriptor")

    private val unaryCallImpl = MemberName(PACKAGE_RPC, "unaryCallImplementation")
    private val serverStreamingCallImpl = MemberName(PACKAGE_RPC, "serverSideStreamingCallImplementation")

    private val promise: ClassName = ClassName("kotlin.js", "Promise")

    override fun applyToClass(
        builder: TypeSpec.Builder,
        service: ProtoService
    ) {
        super.applyToClass(builder, service)

        builder.apply {
            addProperty(
                PropertySpec
                    .builder(
                        "client",
                        service.jsServiceClassName,
                        KModifier.PRIVATE,
                    )
                    .initializer(
                        CodeBlock.of(
                            "%1T(%2N.connectionString, %2N)",
                            service.jsServiceClassName,
                            Const.Service.CHANNEL_PROPERTY_NAME
                        )
                    )
                    .build()
            )

            overrideWithDeadlineAfter(builder, service.className)

            addType(generateJsServiceBridge(service))
        }
    }

    override fun applyToRpcFunction(
        builder: FunSpec.Builder,
        rpc: ProtoRpc
    ) {
        builder.addCode(
            CodeBlock.builder()
                .apply {
                    val clientCallCode = CodeBlock
                        .builder()
                        .add("client.${rpc.name}(")
                        .add("request, ")
                        .add("jsMetadata")
                        .add(")")
                        .build()

                    val rpcImplMember = if (rpc.isReceivingStream) {
                        serverStreamingCallImpl
                    } else {
                        unaryCallImpl
                    }

                    add("return·%M(channel, this.%N + metadata)·{·jsMetadata·->\n", rpcImplMember, Const.Service.CALL_OPTIONS_PROPERTY_NAME)

                    indent()
                    add(clientCallCode)
                    unindent()

                    add("\n}")
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

    private fun generateJsServiceBridge(service: ProtoService): TypeSpec {
        return TypeSpec
            .classBuilder(service.jsServiceClassName)
            .addModifiers(KModifier.PRIVATE)
            .primaryConstructor(
                FunSpec
                    .constructorBuilder()
                    .addParameter("hostname", String::class)
                    .addParameter(
                        ParameterSpec
                            .builder("channel", kmChannel)
                            .build()
                    )
                    .build()
            )
            .addProperty(
                PropertySpec
                    .builder("client", grpcWebClientBase, KModifier.PRIVATE)
                    .initializer("%T(channel.clientOptions)", grpcWebClientBase)
                    .build()
            )
            .addProperty(
                PropertySpec
                    .builder("hostname", String::class, KModifier.PRIVATE)
                    .initializer("hostname")
                    .build()
            )
            .apply {
                service.rpcs.forEach { rpc ->
                    addProperty(
                        PropertySpec
                            .builder(rpc.jsMethodDescriptorName, methodDescriptor, KModifier.PRIVATE)
                            .initializer(CodeBlock.builder().apply {
                                add(
                                    "%T(%S, %S, ::%T, ::%T, {·request:·%T -> request.serialize() }, %T.Companion::deserialize)",
                                    methodDescriptor,
                                    "/${service.file.`package`}.${service.name}/${rpc.name}",
                                    if (rpc.isReceivingStream) {
                                        "server_streaming"
                                    } else "unary",
                                    rpc.sendType.resolve(),
                                    rpc.returnType.resolve(),
                                    rpc.sendType.resolve(),
                                    rpc.returnType.resolve()
                                )
                                add(
                                    ".apply{·methodType·=·%S·}",
                                    if (rpc.isReceivingStream) "server_streaming" else "unary"
                                )
                            }.build())
                            .build()
                    )

                    addFunction(
                        FunSpec
                            .builder(rpc.name)
                            .addParameter("request", rpc.sendType.resolve())
                            .addParameter("metadata", Dynamic)
                            .apply {
                                addCode(
                                    "return client.%N(\"\$hostname/%L/%L\", request, metadata ?: js(%S), %N",
                                    if (rpc.isReceivingStream) {
                                        "serverStreaming"
                                    } else "unaryCall",
                                    "${service.file.`package`}.${service.name}",
                                    rpc.name,
                                    "{}",
                                    rpc.jsMethodDescriptorName
                                )

                                if (rpc.isReceivingStream) {
                                    returns(Dynamic)
                                } else {
                                    returns(promise.parameterizedBy(rpc.returnType.resolve()))
                                }

                                addCode(")")
                            }
                            .build()
                    )
                }
            }
            .build()
    }
}