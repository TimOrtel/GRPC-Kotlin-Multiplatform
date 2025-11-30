package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions

import com.squareup.kotlinpoet.CodeBlock
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SourceTarget
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Property
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage

object UnknownFieldsExtension : SharedFieldExtension() {

    override fun getProperty(message: ProtoMessage, sourceTarget: SourceTarget): Property {
        return Const.Message.Constructor.UnknownFields
    }

    override fun getDefaultValue(message: ProtoMessage, sourceTarget: SourceTarget): CodeBlock {
        return CodeBlock.of("emptyList()")
    }
}
