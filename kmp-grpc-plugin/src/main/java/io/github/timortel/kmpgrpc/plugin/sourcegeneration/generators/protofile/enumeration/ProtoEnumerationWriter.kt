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
            .enumBuilder(protoEnum.className)
            .addModifiers(protoEnum.visibility.modifier)
            .addSuperinterface(kmEnum)
            .addProperty(
                PropertySpec
                    .builder(Const.Enum.NUMBER_PROPERTY_NAME, INT, KModifier.OVERRIDE)
                    .apply {
                        if (supplyImplementation) initializer(Const.Enum.NUMBER_PROPERTY_NAME)
                        addModifiers(this@ProtoEnumerationWriter.modifiers)
                    }
                    .build()
            )
            .apply {
                if (supplyImplementation) {
                    primaryConstructor(
                        FunSpec
                            .constructorBuilder()
                            .addParameter(Const.Enum.NUMBER_PROPERTY_NAME, INT)
                            .build()
                    )

                    addModifiers(this@ProtoEnumerationWriter.modifiers)
                }

                protoEnum.fields.forEach { enumField ->
                    addEnumConstant(
                        enumField.name,
                        TypeSpec
                            .anonymousClassBuilder()
                            .apply {
                                if (supplyImplementation) {
                                    addSuperclassConstructorParameter("%L", enumField.number)
                                }

                                if (Options.Basic.deprecated.get(enumField)) {
                                    addAnnotation(DefaultAnnotations.Deprecated)
                                }
                            }
                            .build()
                    )
                }

                // unrecognizedEnumField
                addEnumConstant(
                    name = ProtoEnum.UNRECOGNIZED_FIELD_NAME,
                    typeSpec = TypeSpec
                        .anonymousClassBuilder()
                        .apply {
                            if (supplyImplementation) {
                                addSuperclassConstructorParameter("-1")
                            }
                        }
                        .build()
                )
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
                                add("else -> %N\n", ProtoEnum.UNRECOGNIZED_FIELD_NAME)
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
