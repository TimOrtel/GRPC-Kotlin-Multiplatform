package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.service

import com.squareup.kotlinpoet.*
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.kmChannel
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.service.ProtoService

abstract class ActualProtoServiceWriter : ProtoServiceWriter(true) {

    abstract val callOptionsType: TypeName

    /**
    * Create empty call options for your platform, e.g., CallOptions.empty() or CallOptions()
    */
    abstract val createEmptyCallOptionsCode: CodeBlock

    override fun applyToClass(
        builder: TypeSpec.Builder,
        service: ProtoService
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
                    .returns(service.className)
                    .addStatement(
                        "return %T(%N, %N)",
                        service.className,
                        Const.Service.CHANNEL_PROPERTY_NAME,
                        Const.Service.CALL_OPTIONS_PROPERTY_NAME
                    )
                    .build()
            )
        }

        overrideWithDeadlineAfter(builder, service.className)
    }

    override fun applyToChannelConstructor(builder: FunSpec.Builder, service: ProtoService) {
        //Fill the call options with the default call options
        builder.apply {
            callThisConstructor(
                CodeBlock.of("%N", Const.Service.Constructor.CHANNEL_PARAMETER_NAME),
                createEmptyCallOptionsCode
            )
        }
    }

    private fun overrideWithDeadlineAfter(builder: TypeSpec.Builder, serviceClass: ClassName) {
        builder.addFunction(
            FunSpec.builder(Const.Service.Functions.WithDeadlineAfter.NAME)
                .addModifiers(KModifier.OVERRIDE, KModifier.ACTUAL)
                .addParameter(Const.Service.Functions.WithDeadlineAfter.ParamDuration.toParamSpec())
                .returns(serviceClass)
                .addStatement(
                    "return super.%N(%N)",
                    Const.Service.Functions.WithDeadlineAfter.NAME,
                    Const.Service.Functions.WithDeadlineAfter.ParamDuration.toParamSpec(),
                )
                .build()
        )
    }
}
