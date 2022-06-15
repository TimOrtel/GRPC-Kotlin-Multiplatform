package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.service

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoRpc
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoService
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.kmAndroidJVMStub
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.kmStub

object JvmServiceWriter : ServiceWriter(true) {

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
                PropertySpec.builder(
                    Const.Service.JVM.PROPERTY_JVM_IMPL,
                    Const.Service.JVM.nativeServiceClassName(protoFile, service),
                    KModifier.LATEINIT,
                    KModifier.OVERRIDE
                )
                    .mutable(true)
                    .build()
            )

            addFunction(
                FunSpec.constructorBuilder()
                    .addParameter("stub", Const.Service.JVM.nativeServiceClassName(protoFile, service))
                    .callThisConstructor()
                    .addStatement("%N = %N", Const.Service.JVM.PROPERTY_JVM_IMPL, "stub")
                    .build()
            )

            overrideWithDeadlineAfter(builder, serviceName)

            addFunction(
                FunSpec
                    .builder("build")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("impl", Const.Service.JVM.nativeServiceClassName(protoFile, service))
                    .returns(serviceName)
                    .addStatement("return %T(impl)", serviceName)
                    .build()
            )
        }
    }

    override fun applyToChannelConstructor(builder: FunSpec.Builder, protoFile: ProtoFile, service: ProtoService) {
        builder.addStatement(
            "%N = %T(channel.managedChannel)",
            Const.Service.JVM.PROPERTY_JVM_IMPL,
            Const.Service.JVM.nativeServiceClassName(
                protoFile,
                service
            )
        )

        builder.callThisConstructor()
    }

    override fun applyToRpcFunction(
        builder: FunSpec.Builder,
        service: ProtoService,
        rpc: ProtoRpc
    ) {
        builder.apply {
            beginControlFlow(
                "return %M",
                MemberName("io.github.timortel.kotlin_multiplatform_grpc_lib.rpc", "simpleCallImplementation")
            )
            if (!rpc.isResponseStream) {
                addCode("%M(", Const.Message.CommonFunction.JVM.commonFunction(rpc.response.jvmType))
            }

            addCode("%N.${rpc.rpcName}(", Const.Service.JVM.PROPERTY_JVM_IMPL)

            if (rpc.request.doDiffer) {
                addCode("request.%N, ", Const.Message.Constructor.JVM.PARAM_IMPL)
            } else {
                addCode("request, ")
            }

            addCode("metadata.%M)", MemberName("io.github.timortel.kotlin_multiplatform_grpc_lib", "jvmMetadata"))

            //Maybe map response to common type
            if (rpc.isResponseStream) {
                addCode(
                    ".%M { %M(it) }\n",
                    MemberName("kotlinx.coroutines.flow", "map"),
                    Const.Message.CommonFunction.JVM.commonFunction(rpc.response.jvmType)
                )
            } else {
                addCode(")\n")
            }
            endControlFlow()
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
        builder.addSuperinterface(
            kmAndroidJVMStub.parameterizedBy(
                serviceClass,
                Const.Service.JVM.nativeServiceClassName(protoFile, service)
            )
        )
    }
}