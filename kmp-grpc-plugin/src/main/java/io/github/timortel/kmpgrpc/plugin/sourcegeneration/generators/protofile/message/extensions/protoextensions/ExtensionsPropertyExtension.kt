package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions.protoextensions

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SourceTarget
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Property
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.kmMessageExtensions
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions.SharedFieldExtension
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage

object ExtensionsPropertyExtension : SharedFieldExtension() {

    override fun getProperty(message: ProtoMessage, sourceTarget: SourceTarget): Property {
        return Const.Message.Constructor.MessageExtensions.parametrizedBy(message.className)
    }

    override fun getDefaultValue(message: ProtoMessage, sourceTarget: SourceTarget): CodeBlock {
        return CodeBlock.of("%T()", kmMessageExtensions)
    }

    override fun getExtraPropertyModifiers(message: ProtoMessage, sourceTarget: SourceTarget): List<KModifier> {
        return listOf(KModifier.OVERRIDE)
    }

    override fun appliesTo(message: ProtoMessage, sourceTarget: SourceTarget): Boolean {
        return message.isExtendable
    }
}
