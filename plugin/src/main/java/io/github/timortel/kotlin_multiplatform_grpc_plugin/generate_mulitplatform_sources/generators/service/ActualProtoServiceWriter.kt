package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.service

import com.squareup.kotlinpoet.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoService
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.kmChannel

abstract class ActualProtoServiceWriter : ProtoServiceWriter(true) {

    abstract val callOptionsType: TypeName

    /**
    * Create empty call options for your platform, e.g. CallOptions.empty() or CallOptions()
    */
    abstract val createEmptyCallOptionsCode: CodeBlock

    override fun applyToClass(
        builder: TypeSpec.Builder,
        protoFile: ProtoFile,
        service: ProtoService,
        serviceClass: ClassName
    ) {
        builder.apply {
            primaryConstructor(
                FunSpec
                    .constructorBuilder()
                    .addParameter(Const.Service.CHANNEL_PROPERTY_NAME, kmChannel)
                    .addParameter(Const.Service.CALL_OPTIONS_PROPERTY_NAME, callOptionsType)
                    .build()
            )

            addProperty(
                PropertySpec.builder(
                    Const.Service.CHANNEL_PROPERTY_NAME,
                    kmChannel,
                    KModifier.OVERRIDE
                )
                    .initializer(Const.Service.CHANNEL_PROPERTY_NAME)
                    .build()
            )

            addProperty(
                PropertySpec
                    .builder(
                        Const.Service.CALL_OPTIONS_PROPERTY_NAME,
                        callOptionsType,
                        KModifier.OVERRIDE
                    )
                    .initializer(Const.Service.CALL_OPTIONS_PROPERTY_NAME)
                    .build()
            )

            addFunction(
                FunSpec
                    .builder("build")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter(Const.Service.CHANNEL_PROPERTY_NAME, kmChannel)
                    .addParameter(Const.Service.CALL_OPTIONS_PROPERTY_NAME, callOptionsType)
                    .returns(serviceClass)
                    .addStatement(
                        "return %T(%N, %N)",
                        serviceClass,
                        Const.Service.CHANNEL_PROPERTY_NAME,
                        Const.Service.CALL_OPTIONS_PROPERTY_NAME
                    )
                    .build()
            )
        }
    }

    override fun applyToChannelConstructor(builder: FunSpec.Builder, protoFile: ProtoFile, service: ProtoService) {
        //Fill the call options with the default call options
        builder.apply {
            callThisConstructor(
                CodeBlock.of("%N", Const.Service.Constructor.CHANNEL_PARAMETER_NAME),
                createEmptyCallOptionsCode
            )
        }
    }
}