package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.enumeration

import com.squareup.kotlinpoet.KModifier

object CommonProtoEnumerationWriter : ProtoEnumerationWriter(isActual = false) {
    override val modifiers: List<KModifier> = emptyList()
}
