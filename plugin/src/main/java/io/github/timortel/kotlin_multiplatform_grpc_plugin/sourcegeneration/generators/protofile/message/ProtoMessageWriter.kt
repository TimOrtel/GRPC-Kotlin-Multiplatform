package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.protofile.message

import com.squareup.kotlinpoet.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.constants.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.protofile.enumeration.ProtoEnumerationWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.protofile.field.ProtoFieldWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.protofile.oneof.ProtoOneOfWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.service.ProtoServiceWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.constants.kmMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.message.ProtoMessageProperty
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.message.field.ProtoFieldCardinality
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.util.joinToCodeBlock

/**
 * Generates the kotlin code for the .proto files.
 */
abstract class ProtoMessageWriter(private val isActual: Boolean) {

    abstract val protoFieldWriter: ProtoFieldWriter

    abstract val protoOneOfWriter: ProtoOneOfWriter

    abstract val protoEnumerationWriter: ProtoEnumerationWriter

    abstract val protoServiceWriter: ProtoServiceWriter

    open val additionalSuperinterfaces: List<TypeName> = emptyList()

    open fun modifyTopLevelFile(builder: FileSpec.Builder, file: ProtoFile) = Unit

    /**
     * Recursive function that adds a proto message class.
     */
    fun generateProtoMessageClass(message: ProtoMessage): TypeSpec {
        val isNested = message.isNested

        return TypeSpec
            .classBuilder(message.className)
            .apply {
                when {
                    isActual -> addModifiers(KModifier.ACTUAL)
                    !isNested -> addModifiers(KModifier.EXPECT)
                    else -> {}
                }

                addSuperinterface(kmMessage)
                addSuperinterfaces(additionalSuperinterfaces)

                message.fields.forEach { field ->
                    protoFieldWriter.addMessageField(this, field)
                }

                message.mapFields.forEach { mapField ->
                    protoFieldWriter.addMapField(this, mapField)
                }

                message.oneOfs.forEach { oneOf ->
                    protoOneOfWriter.generateMethodsAndClasses(this, oneOf)
                }

                applyToClass(this, message)

                // Write child messages
                message.messages.forEach { childMessage ->
                    addType(generateProtoMessageClass(childMessage))
                }

                // Write child enums
                message.enums.forEach { childEnum ->
                    addType(protoEnumerationWriter.generateProtoEnum(childEnum))
                }

                // Write eq function
                addFunction(
                    FunSpec.builder(Const.Message.BasicFunctions.EqualsFunction.NAME)
                        .addModifiers(
                            if (isActual) KModifier.ACTUAL else KModifier.EXPECT,
                            KModifier.OVERRIDE
                        )
                        .addParameter(
                            Const.Message.BasicFunctions.EqualsFunction.OTHER_PARAM,
                            ANY.copy(nullable = true)
                        )
                        .returns(BOOLEAN)
                        .apply {
                            applyToEqualsFunction(this, message)
                        }
                        .build()
                )

                addFunction(
                    FunSpec.builder(Const.Message.BasicFunctions.HashCodeFunction.NAME)
                        .returns(INT)
                        .addModifiers(
                            if (isActual) KModifier.ACTUAL else KModifier.EXPECT,
                            KModifier.OVERRIDE
                        )
                        .apply {
                            applyToHashCodeFunction(this, message)
                        }
                        .build()
                )
            }
            .build()
    }

    abstract fun applyToClass(builder: TypeSpec.Builder, message: ProtoMessage)

    open fun applyToEqualsFunction(builder: FunSpec.Builder, message: ProtoMessage) {
        if (isActual) {
            builder.apply {
                val otherParamName = Const.Message.BasicFunctions.EqualsFunction.OTHER_PARAM

                addStatement("if (%N === this) return true", otherParamName)
                addStatement("if (%N !is %T) return false", otherParamName, message.className)

                val separator = "\n"

                val fieldsCodeBlock = message.fields.joinToCodeBlock(separator) { field ->
                    add("if (")
                    add(
                        field.type.inequalityCode(
                            attributeName = field.attributeName,
                            otherParamName = otherParamName,
                            isRepeated = field.cardinality == ProtoFieldCardinality.Repeated
                        )
                    )
                    add(") return false")
                }

                val mapFieldCodeBlock = message.mapFields.joinToCodeBlock(separator) { mapField ->
                    add(
                        "if (%1N != %2N.%1N) return false",
                        mapField.attributeName,
                        otherParamName
                    )
                }

                // Assume that each one of sealed class has their equals method set properly
                val oneOfCodeBlock = message.oneOfs.joinToCodeBlock(separator) { oneOf ->
                    add(
                        "if (%1N != %2N.%1N) return false",
                        oneOf.attributeName,
                        otherParamName
                    )
                }

                addCode(
                    listOf(fieldsCodeBlock, mapFieldCodeBlock, oneOfCodeBlock)
                        .filter { it.isNotEmpty() }
                        .joinToCodeBlock(separator) { add(it) }
                )

                addCode("\n")

                addStatement("return true")
            }
        }
    }

    open fun applyToHashCodeFunction(builder: FunSpec.Builder, message: ProtoMessage) {
        if (isActual) {
            val properties: List<ProtoMessageProperty> = message.fields + message.mapFields + message.oneOfs

            builder.apply {
                if (properties.isEmpty()) {
                    addStatement("return 0")
                    return
                }

                if (properties.size == 1) {
                    addStatement("return %N.hashCode()", properties.first().attributeName)
                    return
                }

                properties.forEachIndexed { index, property ->
                    val attrName = property.attributeName

                    //Mimic the way IntelliJ generates hashCode
                    if (index == 0) {
                        addStatement("var result = %N.hashCode()", attrName)
                    } else {
                        addStatement("result = 31 * result + %N.hashCode()", attrName)
                    }
                }

                addStatement("return result")
            }
        }
    }
}
