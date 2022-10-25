package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.service

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoRpc
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoService
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const

object JvmServiceWriter : IosJvmServiceWriter() {

    override val classAndFunctionModifiers: List<KModifier> = listOf(KModifier.ACTUAL)
    override val channelConstructorModifiers: List<KModifier> = listOf(KModifier.ACTUAL)
    override val primaryConstructorModifiers: List<KModifier> = listOf(KModifier.PRIVATE, KModifier.ACTUAL)

    private val METHOD_DESCRIPTOR = ClassName("io.grpc", "MethodDescriptor")
    private val METHOD_TYPE = METHOD_DESCRIPTOR.nestedClass("MethodType")
    private val PROTO_LITE_UTILS = ClassName("io.grpc.protobuf.lite", "ProtoLiteUtils")

    private val CLIENT_CALLS = ClassName("io.grpc.kotlin", "ClientCalls")

    override val callOptionsType: TypeName = ClassName("io.grpc", "CallOptions")
    override val createEmptyCallOptionsCode: CodeBlock = CodeBlock.of("%T.DEFAULT", callOptionsType)


    override fun applyToClass(
        builder: TypeSpec.Builder,
        protoFile: ProtoFile,
        service: ProtoService,
        serviceName: ClassName
    ) {
        super.applyToClass(builder, protoFile, service, serviceName)

        builder.apply {
            addSuperinterface(ClassName(PACKAGE_STUB, "AndroidJvmKMStub").parameterizedBy(serviceName))

            overrideWithDeadlineAfter(builder, serviceName)

            addType(
                TypeSpec
                    .companionObjectBuilder()
                    .apply {
                        //For each rpc, generate a MethodDescriptor
                        service.rpcs.forEach { rpc ->
                            addProperty(
                                PropertySpec
                                    .builder(
                                        Const.Service.JVM.Companion.methodDescriptorPropertyName(service, rpc),
                                        METHOD_DESCRIPTOR.parameterizedBy(
                                            rpc.request.commonType,
                                            rpc.response.commonType
                                        )
                                    )
                                    .initializer(
                                        CodeBlock.builder().apply {
                                            val methodType = when (rpc.method) {
                                                ProtoRpc.Method.UNARY -> "UNARY"
                                                ProtoRpc.Method.SERVER_STREAMING -> "SERVER_STREAMING"
                                            }

                                            val fullMethodName =
                                                "${protoFile.pkg}.${service.serviceName}/${rpc.rpcName}"

                                            addStatement(
                                                "%T.newBuilder<%T, %T>()",
                                                METHOD_DESCRIPTOR,
                                                rpc.request.commonType,
                                                rpc.response.commonType
                                            )
                                            add(".setType(%T.%N)", METHOD_TYPE, methodType)
                                            add(".setFullMethodName(%S)", fullMethodName)
                                            add(".setSampledToLocalTracing(true)")
                                            add(
                                                ".setRequestMarshaller(%T.marshaller(%T()))",
                                                PROTO_LITE_UTILS,
                                                rpc.request.commonType
                                            )
                                            add(
                                                ".setResponseMarshaller(%T.marshaller(%T()))",
                                                PROTO_LITE_UTILS,
                                                rpc.response.commonType
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
        protoFile: ProtoFile,
        service: ProtoService,
        rpc: ProtoRpc
    ) {
        val jvmMetadataMember = MemberName("io.github.timortel.kotlin_multiplatform_grpc_lib", "jvmMetadata")

        builder.apply {
            val funName = when (rpc.method) {
                ProtoRpc.Method.UNARY -> "unaryRpc"
                ProtoRpc.Method.SERVER_STREAMING -> "serverStreamingRpc"
            }

            addCode("return %T.%N(", CLIENT_CALLS, funName)
            addCode("channel = %N.managedChannel,", Const.Service.IosJvm.CHANNEL_PROPERTY_NAME)
            addCode("callOptions = %N,", Const.Service.IosJvm.CALL_OPTIONS_PROPERTY_NAME)
            addCode("method = %N,", Const.Service.JVM.Companion.methodDescriptorPropertyName(service, rpc))
            addCode("headers = metadata.%M,", jvmMetadataMember)
            addCode("request = %N", Const.Service.RpcCall.PARAM_REQUEST)
            addCode(")")
        }
    }

    override fun applyToMetadataParameter(builder: ParameterSpec.Builder, service: ProtoService) = Unit

    override fun specifyInheritance(
        builder: TypeSpec.Builder,
        serviceClass: ClassName,
        protoFile: ProtoFile,
        service: ProtoService
    ) {
        builder.superclass(kmStub.parameterizedBy(serviceClass))
    }
}