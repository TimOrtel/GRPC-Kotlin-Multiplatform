package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.enumeration

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.EnumCompanion
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.kmEnum
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.DefaultAnnotations
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option.Options
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoEnum

abstract class ProtoEnumerationWriter(val isActual: Boolean) {

    abstract val modifiers: List<KModifier>

    fun generateProtoEnum(
        protoEnum: ProtoEnum
    ): TypeSpec {
        /*
         * Top level enums do not need actual and expect declarations, while nested enums do need them
         */
        val supplyImplementation = isActual || !protoEnum.isNested

        return TypeSpec
            .interfaceBuilder(protoEnum.className)
            .addModifiers(protoEnum.visibility.modifier)
            .addModifiers(KModifier.SEALED)
            .addSuperinterface(kmEnum)
            .apply {
                if (supplyImplementation) {
                    addModifiers(this@ProtoEnumerationWriter.modifiers)
                }

                protoEnum.fields.forEach { enumField ->
                    addType(
                        TypeSpec.objectBuilder(enumField.className)
                            .addModifiers(KModifier.DATA)
                            .addModifiers(this@ProtoEnumerationWriter.modifiers)
                            .addProperty(
                                PropertySpec
                                    .builder(Const.Enum.NUMBER_PROPERTY_NAME, INT, KModifier.OVERRIDE)
                                    .apply {
                                        if (supplyImplementation) initializer("%L", enumField.number)
                                        addModifiers(this@ProtoEnumerationWriter.modifiers)

                                        if (Options.Basic.deprecated.get(enumField)) {
                                            addAnnotation(DefaultAnnotations.Deprecated)
                                        }
                                    }
                                    .build()
                            )
                            .addSuperinterface(protoEnum.className)
                            .build()
                    )
                }

                // unrecognizedEnumField
                if (protoEnum.isOpen(protoEnum.file.languageVersion)) {
                    addType(
                        TypeSpec
                            .classBuilder(protoEnum.unrecognizedSubtypeClassName)
                            .addModifiers(this@ProtoEnumerationWriter.modifiers)
                            .addProperty(
                                PropertySpec.builder(Const.Enum.NUMBER_PROPERTY_NAME, INT, KModifier.OVERRIDE)
                                    .addModifiers(this@ProtoEnumerationWriter.modifiers)
                                    .apply {
                                        if (supplyImplementation) initializer("%N", Const.Enum.NUMBER_PROPERTY_NAME)
                                    }
                                    .build()
                            )
                            .primaryConstructor(
                                FunSpec.constructorBuilder()
                                    .addParameter(Const.Enum.NUMBER_PROPERTY_NAME, INT)
                                    .addModifiers(this@ProtoEnumerationWriter.modifiers)
                                    .build()
                            )
                            .addSuperinterface(protoEnum.className)
                            .apply {
                                if (supplyImplementation) addModifiers(KModifier.DATA)
                            }
                            .build()
                    )
                }
            }
            //The method that will return the correct enum for the given num
            .addType(
                TypeSpec
                    .companionObjectBuilder()
                    .addSuperinterface(EnumCompanion.parameterizedBy(protoEnum.className))
                    .apply {
                        if (isActual) addModifiers(KModifier.ACTUAL)
                    }
                    .addFunction(
                        buildGetEnumForNumberFunction(
                            protoEnum = protoEnum,
                            orNullVersion = false,
                            supplyImplementation = supplyImplementation
                        )
                    )
                    .addFunction(
                        buildGetEnumForNumberFunction(
                            protoEnum = protoEnum,
                            orNullVersion = true,
                            supplyImplementation = supplyImplementation
                        )
                    )
                    .build()
            )
            .build()

    }

    private fun buildGetEnumForNumberFunction(
        protoEnum: ProtoEnum,
        orNullVersion: Boolean,
        supplyImplementation: Boolean
    ): FunSpec = FunSpec
        .builder(if (orNullVersion) Const.Enum.GET_ENUM_FOR_OR_NULL_FUNCTION_NAME else Const.Enum.GET_ENUM_FOR_FUNCTION_NAME)
        .addModifiers(KModifier.OVERRIDE)
        .returns(protoEnum.className.copy(nullable = orNullVersion))
        .addParameter("num", INT)
        .apply {
            if (supplyImplementation) {
                addCode(
                    CodeBlock
                        .builder()
                        .add("return ")
                        .beginControlFlow("when(num)")
                        .apply {
                            protoEnum.fields.forEach { field ->
                                add("%L -> %N\n", field.number, field.name)
                            }
                            if (orNullVersion) {
                                add("else -> null\n")
                            } else {
                                if (protoEnum.isOpen(protoEnum.file.languageVersion)) {
                                    add("else -> %T(num)\n", protoEnum.unrecognizedSubtypeClassName)
                                } else {
                                    add(
                                        "else -> throw %T(%P)",
                                        IllegalArgumentException::class.asClassName(),
                                        $$"Unknown numeric value $num for closed enum $${protoEnum.name}."
                                    )
                                }
                            }
                        }
                        .endControlFlow()
                        .build()
                )

                addModifiers(this@ProtoEnumerationWriter.modifiers)
            }
        }
        .build()
}
