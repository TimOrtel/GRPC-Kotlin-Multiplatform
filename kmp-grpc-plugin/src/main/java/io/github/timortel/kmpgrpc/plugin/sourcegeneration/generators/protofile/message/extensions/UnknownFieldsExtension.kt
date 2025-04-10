package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SourceTarget
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage

object UnknownFieldsExtension : MessageWriterExtension {
    override fun applyToConstructor(builder: FunSpec.Builder, message: ProtoMessage, sourceTarget: SourceTarget) {
        builder.addParameter(
            Const.Message.Constructor.UnknownFields.toParamSpecBuilder()
                .apply {
                    if (sourceTarget !is SourceTarget.Actual) {
                        defaultValue("emptyList()")
                    }
                }
                .build()
        )
    }

    override fun applyToClass(builder: TypeSpec.Builder, message: ProtoMessage, sourceTarget: SourceTarget) {
        val modifiers = if (sourceTarget is SourceTarget.Actual) arrayOf(KModifier.ACTUAL) else emptyArray()

        builder.addProperty(
            Const.Message.Constructor.UnknownFields.toPropertySpecBuilder(*modifiers)
                .apply {
                    if (sourceTarget is SourceTarget.Actual) {
                        initializer("%N", Const.Message.Constructor.UnknownFields.name)
                    }
                }
                .build()
        )
    }
}
