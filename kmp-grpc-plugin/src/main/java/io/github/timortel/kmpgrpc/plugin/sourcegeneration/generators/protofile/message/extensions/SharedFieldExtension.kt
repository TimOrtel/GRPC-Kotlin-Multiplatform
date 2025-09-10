package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SourceTarget
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Property
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage

/**
 * Base extension that adds a property to the constructor and to the class.
 */
abstract class SharedFieldExtension : MessageWriterExtension {

    abstract fun getProperty(message: ProtoMessage, sourceTarget: SourceTarget): Property
    abstract fun getDefaultValue(message: ProtoMessage, sourceTarget: SourceTarget): CodeBlock

    open fun getExtraPropertyModifiers(message: ProtoMessage, sourceTarget: SourceTarget): List<KModifier> = emptyList()

    override fun applyToConstructor(builder: FunSpec.Builder, message: ProtoMessage, sourceTarget: SourceTarget) {
        builder.addParameter(
            getProperty(message, sourceTarget).toParamSpecBuilder()
                .apply {
                    if (sourceTarget !is SourceTarget.Actual) {
                        defaultValue(getDefaultValue(message, sourceTarget))
                    }
                }
                .build()
        )
    }

    override fun applyToClass(builder: TypeSpec.Builder, message: ProtoMessage, sourceTarget: SourceTarget) {
        val modifiers = if (sourceTarget is SourceTarget.Actual) arrayOf(KModifier.ACTUAL) else emptyArray()

        val property = getProperty(message, sourceTarget)
        builder.addProperty(
            property.toPropertySpecBuilder(*(modifiers + getExtraPropertyModifiers(message, sourceTarget)))
                .apply {
                    if (sourceTarget is SourceTarget.Actual) {
                        initializer("%N", property.name)
                    }
                }
                .build()
        )
    }
}
