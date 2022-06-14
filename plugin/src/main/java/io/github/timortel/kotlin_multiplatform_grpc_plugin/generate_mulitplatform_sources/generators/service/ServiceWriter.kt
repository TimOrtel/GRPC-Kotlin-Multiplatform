package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.service

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kotlin_multiplatform_grpc_lib.KMChannel
import io.github.timortel.kotlin_multiplatform_grpc_lib.KMMetadata
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoRpc
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoService
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import kotlinx.coroutines.flow.Flow
import java.io.File

abstract class ServiceWriter(private val isActual: Boolean) {

    protected abstract val classAndFunctionModifiers: List<KModifier>

    protected abstract val primaryConstructorModifiers: List<KModifier>
    protected abstract val channelConstructorModifiers: List<KModifier>

    fun writeServiceStub(protoFile: ProtoFile, service: ProtoService, outputFolder: File) {
        val serviceFileName = Const.Service.getName(service)

        val serviceClass = ClassName(protoFile.pkg, serviceFileName)

        FileSpec
            .builder(protoFile.pkg, serviceFileName)
            .addType(
                TypeSpec
                    .classBuilder(serviceFileName)
                    .addModifiers(classAndFunctionModifiers)
                    .primaryConstructor(FunSpec.constructorBuilder().addModifiers(primaryConstructorModifiers).build())
                    .addFunction(
                        FunSpec
                            .constructorBuilder()
                            .addParameter("channel", KMChannel::class)
                            .addModifiers(channelConstructorModifiers)
                            .apply { applyToChannelConstructor(this, protoFile, service) }
                            .build()
                    )
                    .apply {
                        specifyInheritance(this, serviceClass, protoFile, service)
                        applyToClass(this, protoFile, service, serviceClass)
                        service.rpcs.forEach { rpc ->
                            val returnType = if (rpc.isResponseStream) {
                                Flow::class.asTypeName().parameterizedBy(rpc.response.commonType)
                            } else rpc.response.commonType

                            addFunction(
                                FunSpec
                                    .builder(rpc.rpcName)
                                    .addModifiers(KModifier.SUSPEND)
                                    .addModifiers(classAndFunctionModifiers)
                                    .addParameter(Const.Service.RpcCall.PARAM_REQUEST, rpc.request.commonType)
                                    .addParameter(
                                        ParameterSpec
                                            .builder(Const.Service.RpcCall.PARAM_METADATA, KMMetadata::class)
                                            .apply { applyToMetadataParameter(this, service) }
                                            .build()
                                    )
                                    .returns(returnType)
                                    .apply {
                                        applyToRpcFunction(this, service, rpc)
                                    }
                                    .build()
                            )
                        }
                    }
                    .build()
            )
            .build()
            .writeTo(outputFolder)
    }

    protected abstract fun applyToClass(
        builder: TypeSpec.Builder,
        protoFile: ProtoFile,
        service: ProtoService,
        serviceName: ClassName
    )

    /**
     * Apply to the constructor that receives the channel argument. That is not the primary constructor.
     */
    protected abstract fun applyToChannelConstructor(
        builder: FunSpec.Builder,
        protoFile: ProtoFile,
        service: ProtoService
    )

    protected abstract fun applyToRpcFunction(builder: FunSpec.Builder, service: ProtoService, rpc: ProtoRpc)

    protected abstract fun applyToMetadataParameter(builder: ParameterSpec.Builder, service: ProtoService)

    protected abstract fun specifyInheritance(
        builder: TypeSpec.Builder,
        serviceClass: ClassName,
        protoFile: ProtoFile,
        service: ProtoService
    )
}