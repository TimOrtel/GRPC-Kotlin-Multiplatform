package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.service

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.*
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.service.ProtoRpc
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.service.ProtoService

object JvmProtoServiceWriter : NativeJvmProtoServiceWriter() {

    override val channelConstructorModifiers: List<KModifier> = listOf(KModifier.ACTUAL)
    override val primaryConstructorModifiers: List<KModifier> = listOf(KModifier.PRIVATE, KModifier.ACTUAL)

    private val METHOD_DESCRIPTOR = ClassName("io.grpc", "MethodDescriptor")
    private val METHOD_TYPE = METHOD_DESCRIPTOR.nestedClass("MethodType")

    override val unaryCallMemberName: MemberName = MemberName(PACKAGE_RPC, "unaryRpc")
    override val clientStreamingCallMemberName: MemberName = MemberName(PACKAGE_RPC, "clientStreamingRpc")
    override val serverStreamingCallMemberName: MemberName = MemberName(PACKAGE_RPC, "serverStreamingRpc")
    override val bidiStreamingCallMemberName: MemberName = MemberName(PACKAGE_RPC, "bidiStreamingRpc")

    override val callOptionsType: TypeName = ClassName("io.grpc", "CallOptions")
    override val createEmptyCallOptionsCode: CodeBlock = CodeBlock.of("%T.DEFAULT", callOptionsType)

    private val androidJvmStub = ClassName(PACKAGE_STUB, "AndroidJvmStub")

    override fun applyToClass(
        builder: TypeSpec.Builder,
        service: ProtoService
    ) {
        super.applyToClass(builder, service)

        builder.apply {
            addSuperinterface(androidJvmStub.parameterizedBy(service.className))

            addType(
                TypeSpec
                    .companionObjectBuilder()
                    .apply {
                        //For each rpc, generate a MethodDescriptor
                        service.rpcs.forEach { rpc ->
                            addProperty(
                                PropertySpec
                                    .builder(
                                        rpc.jvmMethodDescriptorName,
                                        METHOD_DESCRIPTOR.parameterizedBy(
                                            rpc.sendType.resolve(),
                                            rpc.returnType.resolve()
                                        )
                                    )
                                    .initializer(
                                        CodeBlock.builder().apply {
                                            val fullMethodName =
                                                "${service.file.`package`.orEmpty()}.${service.name}/${rpc.name}"

                                            addStatement(
                                                "%T.newBuilder<%T, %T>()",
                                                METHOD_DESCRIPTOR,
                                                rpc.sendType.resolve(),
                                                rpc.returnType.resolve()
                                            )
                                            add(".setType(%T.%N)", METHOD_TYPE, rpc.methodType.jvmMethodType)
                                            add(".setFullMethodName(%S)", fullMethodName)
                                            add(".setSampledToLocalTracing(true)")
                                            add(
                                                ".setRequestMarshaller(%T.Companion)",
                                                rpc.sendType.resolve()
                                            )
                                            add(
                                                ".setResponseMarshaller(%T.Companion)",
                                                rpc.returnType.resolve()
                                            )
                                            add(".build()")
                                        }.build()
                                    )
                                    .build()
                            )
                        }
                    }
                    .build()
            )
        }
    }

    override fun applyToRpcFunction(
        builder: FunSpec.Builder,
        rpc: ProtoRpc,
        rpcImplementation: MemberName,
        requestParamName: String
    ) {
        builder.apply {
            addCode("return %M(", rpcImplementation)
            addCode("channel = %N,", Const.Service.CHANNEL_PROPERTY_NAME)
            addCode("callOptions = %N,", Const.Service.CALL_OPTIONS_PROPERTY_NAME)
            addCode("method = %N,", rpc.jvmMethodDescriptorName)
            addCode("headers = metadata,")
            addCode("%N = %N", requestParamName, requestParamName)
            addCode(")")
        }
    }

    override fun applyToMetadataParameter(builder: ParameterSpec.Builder, service: ProtoService) = Unit

    override fun specifyInheritance(
        builder: TypeSpec.Builder,
        service: ProtoService
    ) {
        builder.superclass(kmStub.parameterizedBy(service.className))
    }
}
