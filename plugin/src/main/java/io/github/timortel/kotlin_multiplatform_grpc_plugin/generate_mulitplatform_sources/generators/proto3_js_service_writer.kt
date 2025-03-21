package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators

import com.squareup.kotlinpoet.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoRpc
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoService
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.service.JsProtoServiceWriter
import java.io.File

private val grpcWebClientBase =
    ClassName("io.github.timortel.kotlin_multiplatform_grpc_lib.rpc", "GrpcWebClientBase")
private val clientOptions =
    ClassName("io.github.timortel.kotlin_multiplatform_grpc_lib.rpc", "ClientOptions")
private val methodDescriptor =
    ClassName("io.github.timortel.kotlin_multiplatform_grpc_lib.rpc", "MethodDescriptor")

fun writeJsServiceFile(protoFile: ProtoFile, service: ProtoService, jsOutputFolder: File) {
    JsProtoServiceWriter.writeServiceStub(protoFile, service, jsOutputFolder)

    val getMethodDescriptorName =
        { rpc: ProtoRpc -> "${rpc.rpcName.decapitalize()}MethodDescriptor" }

    //js bridge
    FileSpec
        .builder(
            protoFile.pkg,
            "${protoFile.fileNameWithoutExtension}_${service.serviceName}_service_js_bridge"
        )
        .addType(
            TypeSpec
                .classBuilder(getJSServiceName(service))
                .primaryConstructor(
                    FunSpec
                        .constructorBuilder()
                        .addParameter("hostname", String::class)
                        .addParameter(
                            ParameterSpec
                                .builder("options", clientOptions)
                                .defaultValue("js(%S)", "{}")
                                .build()
                        )
                        .build()
                )
                .addProperty(
                    PropertySpec
                        .builder("client", grpcWebClientBase, KModifier.PRIVATE)
                        .initializer("%T(options)", grpcWebClientBase)
                        .build()
                )
                .addProperty(
                    PropertySpec
                        .builder("hostname", String::class, KModifier.PRIVATE)
                        .initializer("hostname")
                        .build()
                )
                .addInitializerBlock(CodeBlock.builder().apply {
                    addStatement("options.format = %S", "text")
                }.build())
                .apply {
                    service.rpcs.forEach { rpc ->
                        val methodDescriptorName = getMethodDescriptorName(rpc)

                        addProperty(
                            PropertySpec
                                .builder(methodDescriptorName, methodDescriptor, KModifier.PRIVATE)
                                .initializer(CodeBlock.builder().apply {
                                    addStatement(
                                        "%T(%S, %S, ::%T, ::%T, {·request:·%T -> request.serialize() }, %T.Companion::deserialize)",
                                        methodDescriptor,
                                        "/${protoFile.pkg}.${service.serviceName}/${rpc.rpcName}",
                                        when (rpc.method) {
                                            ProtoRpc.Method.UNARY -> "unary"
                                            ProtoRpc.Method.SERVER_STREAMING -> "server_streaming"
                                        },
                                        rpc.request.commonType,
                                        rpc.response.commonType,
                                        rpc.request.commonType,
                                        rpc.response.commonType
                                    )
                                }.build())
                                .build()
                        )

                        addFunction(
                            FunSpec
                                .builder(rpc.rpcName)
                                .addParameter("request", rpc.request.commonType)
                                .addParameter("metadata", Dynamic)
                                .apply {
                                    addCode(
                                        "return client.%N(\"\$hostname/%L/%L\", request, metadata ?: js(%S), %N",
                                        when (rpc.method) {
                                            ProtoRpc.Method.UNARY -> "rpcCall"
                                            ProtoRpc.Method.SERVER_STREAMING -> "serverStreaming"
                                        },
                                        "${protoFile.pkg}.${service.serviceName}",
                                        rpc.rpcName,
                                        "{}",
                                        methodDescriptorName
                                    )

                                    when (rpc.method) {
                                        ProtoRpc.Method.UNARY -> {
                                            addParameter(
                                                "callback",
                                                LambdaTypeName.get(
                                                    parameters =
                                                    arrayOf(
                                                        Dynamic,
                                                        rpc.response.commonType
                                                    ),
                                                    returnType = UNIT
                                                )
                                            )

                                            addCode(", callback)")
                                        }

                                        ProtoRpc.Method.SERVER_STREAMING -> {
                                            returns(Dynamic)
                                            addCode(")")
                                        }
                                    }
                                }
                                .build()
                        )
                    }
                }
                .build()
        )
        .build()
        .writeTo(jsOutputFolder)
}

private fun getJSServiceName(service: ProtoService) = "JS_" + service.serviceName.capitalize()