package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.service

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.PACKAGE_STUB
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.kmStub
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.service.ProtoRpc
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.service.ProtoService

object JvmProtoServiceWriter : ActualProtoServiceWriter() {

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
        service: ProtoService
    ) {
        super.applyToClass(builder, service)

        builder.apply {
            addSuperinterface(ClassName(PACKAGE_STUB, "AndroidJvmKMStub").parameterizedBy(service.className))

            overrideWithDeadlineAfter(builder, service.className)

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
                                            val methodType = if (rpc.isReceivingStream) {
                                                "SERVER_STREAMING"
                                            } else {
                                                "UNARY"
                                            }

                                            val fullMethodName =
                                                "${service.file.`package`.orEmpty()}.${service.name}/${rpc.name}"

                                            addStatement(
                                                "%T.newBuilder<%T, %T>()",
                                                METHOD_DESCRIPTOR,
                                                rpc.sendType.resolve(),
                                                rpc.returnType.resolve()
                                            )
                                            add(".setType(%T.%N)", METHOD_TYPE, methodType)
                                            add(".setFullMethodName(%S)", fullMethodName)
                                            add(".setSampledToLocalTracing(true)")
                                            add(
                                                ".setRequestMarshaller(%T.marshaller(%T()))",
                                                PROTO_LITE_UTILS,
                                                rpc.sendType.resolve()
                                            )
                                            add(
                                                ".setResponseMarshaller(%T.marshaller(%T()))",
                                                PROTO_LITE_UTILS,
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
        rpc: ProtoRpc
    ) {
        val jvmMetadataMember = MemberName("io.github.timortel.kotlin_multiplatform_grpc_lib", "jvmMetadata")

        builder.apply {
            val funName = if (rpc.isReceivingStream) {
                "serverStreamingRpc"
            } else {
               "unaryRpc"
            }

            addCode("return %T.%N(", CLIENT_CALLS, funName)
            addCode("channel = %N.managedChannel,", Const.Service.CHANNEL_PROPERTY_NAME)
            addCode("callOptions = %N,", Const.Service.CALL_OPTIONS_PROPERTY_NAME)
            addCode("method = %N,", rpc.jvmMethodDescriptorName)
            addCode("headers = metadata.%M,", jvmMetadataMember)
            addCode("request = %N", Const.Service.RpcCall.PARAM_REQUEST)
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