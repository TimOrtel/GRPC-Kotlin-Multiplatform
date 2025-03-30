package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.enumeration

import com.squareup.kotlinpoet.KModifier

object ActualProtoEnumerationWriter : ProtoEnumerationWriter(isActual = true) {
    override val modifiers: List<KModifier> = listOf(KModifier.ACTUAL)
}