package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.kmMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.enumeration.ActualProtoEnumerationWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.enumeration.ProtoEnumerationWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.ActualProtoFieldWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.ProtoFieldWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoFieldCardinality

abstract class ActualProtoMessageWriter : ProtoMessageWriter(true) {

    override val protoFieldWriter: ProtoFieldWriter = ActualProtoFieldWriter
    override val protoEnumerationWriter: ProtoEnumerationWriter = ActualProtoEnumerationWriter

    override fun applyToClass(builder: TypeSpec.Builder, message: ProtoMessage) {
        builder.apply {
            primaryConstructor(
                FunSpec
                    .constructorBuilder()
                    .apply {
                        callMessageWriterExtensions(message) { it.applyToConstructor(this, message, target) }

                        //one of attributes do not get a parameter, as they get the one of parameter
                        message.fields.forEach { field ->
                            when (field.cardinality) {
                                is ProtoFieldCardinality.Singular -> {
                                    val type = if (field.type.isMessage) field.type.resolve().copy(nullable = true)
                                    else field.type.resolve()

                                    addParameter(
                                        ParameterSpec
                                            .builder(field.attributeName, type)
                                            .defaultValue(field.type.defaultValue())
                                            .build()
                                    )
                                }

                                ProtoFieldCardinality.Repeated -> {
                                    addParameter(
                                        ParameterSpec
                                            .builder(field.attributeName, LIST.parameterizedBy(field.type.resolve()))
                                            .defaultValue("emptyList()")
                                            .build()
                                    )
                                }
                            }
                        }

                        message.mapFields.forEach { mapField ->
                            addParameter(
                                ParameterSpec
                                    .builder(
                                        mapField.attributeName,
                                        MAP.parameterizedBy(
                                            mapField.keyType.resolve(),
                                            mapField.valuesType.resolve(),
                                        )
                                    )
                                    .defaultValue("emptyMap()")
                                    .build()
                            )
                        }

                        message.oneOfs.forEach { oneOf ->
                            addParameter(
                                ParameterSpec
                                    .builder(
                                        oneOf.attributeName,
                                        oneOf.sealedClassName
                                    )
                                    .defaultValue("%T", oneOf.sealedClassNameNotSet)
                                    .build()
                            )
                        }
                    }
                    .build()
            )

            addSuperinterface(kmMessage)

            addProperty(
                Const.Message.fullNameProperty.toPropertySpecBuilder(KModifier.ACTUAL, KModifier.OVERRIDE)
                    .initializer("%T.%N", message.className, Const.Message.Companion.fullNameProperty.name)
                    .build()
            )
        }
    }


    override fun applyToCompanionObject(builder: TypeSpec.Builder, message: ProtoMessage) {
        builder.apply {
            addModifiers(KModifier.ACTUAL)

            builder.addProperty(
                Const.Message.Companion.fullNameProperty.toPropertySpecBuilder(KModifier.ACTUAL, KModifier.OVERRIDE)
                    .initializer("%S", message.fullName)
                    .build()
            )
        }
    }
}