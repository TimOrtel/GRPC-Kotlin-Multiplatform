package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SourceTarget
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.MessageCompanion
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.MessageDeserializer
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.kmMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.enumeration.ProtoEnumerationWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.ProtoFieldWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions.*
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions.serialization.DeserializationFunctionExtension
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions.serialization.NativeDeserializationFunctionExtension
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions.serialization.RequiredSizePropertyExtension
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions.serialization.SerializationFunctionExtension
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.oneof.ProtoOneOfWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage

/**
 * Generates the kotlin code for the .proto files.
 */
abstract class ProtoMessageWriter(private val isActual: Boolean) {

    abstract val protoFieldWriter: ProtoFieldWriter

    abstract val protoOneOfWriter: ProtoOneOfWriter

    abstract val protoEnumerationWriter: ProtoEnumerationWriter

    abstract val target: SourceTarget

    open val additionalSuperinterfaces: List<TypeName> = emptyList()

    private val extensions: List<MessageWriterExtension> = listOf(
        SerializationFunctionExtension(),
        RequiredSizePropertyExtension(),
        NativeDeserializationFunctionExtension(),
        DeserializationFunctionExtension(),
        EqualsFunctionExtension,
        HashCodeFunctionExtension,
        ToStringFunctionExtension,
        FieldPropertyConstructorExtension,
        UnknownFieldsExtension
    )

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

                primaryConstructor(
                    FunSpec
                        .constructorBuilder()
                        .apply {
                            if (isActual) {
                                addModifiers(KModifier.ACTUAL)
                            }

                            callMessageWriterExtensions(message) { it.applyToConstructor(this, message, target) }
                        }
                        .build()
                )

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

                addType(
                    TypeSpec.companionObjectBuilder()
                        .addSuperinterface(MessageDeserializer.parameterizedBy(message.className))
                        .addSuperinterface(MessageCompanion.parameterizedBy(message.className))
                        .apply {
                            applyToCompanionObject(this, message)

                            callMessageWriterExtensions(message) { it.applyToCompanionObject(this, message, target) }
                        }
                        .build()
                )

                callMessageWriterExtensions(message) { it.applyToClass(this, message, target) }
            }
            .build()
    }

    abstract fun applyToClass(builder: TypeSpec.Builder, message: ProtoMessage)

    abstract fun applyToCompanionObject(builder: TypeSpec.Builder, message: ProtoMessage)

    protected fun callMessageWriterExtensions(message: ProtoMessage, call: (MessageWriterExtension) -> Unit) {
        extensions.filter { it.appliesTo(message, target) }.forEach { call(it) }
    }
}
