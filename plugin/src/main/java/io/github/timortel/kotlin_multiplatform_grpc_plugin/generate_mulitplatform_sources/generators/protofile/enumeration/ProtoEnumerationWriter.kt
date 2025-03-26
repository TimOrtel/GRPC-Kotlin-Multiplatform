package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.enumeration

import com.squareup.kotlinpoet.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.kmEnum
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.ProtoEnum

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
                    .apply {
                        if (isActual) addModifiers(KModifier.ACTUAL)
                    }
                    .addFunction(
                        FunSpec
                            .builder(Const.Enum.getEnumForNumFunctionName)
                            .returns(protoEnum.className)
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
                                                add("else -> %N\n", ProtoEnum.UNRECOGNIZED_FIELD_NAME)
                                            }
                                            .endControlFlow()
                                            .build()
                                    )

                                    addModifiers(this@ProtoEnumerationWriter.modifiers)
                                }
                            }
                            .build()
                    )
                    .build()
            )
            .build()

    }
}