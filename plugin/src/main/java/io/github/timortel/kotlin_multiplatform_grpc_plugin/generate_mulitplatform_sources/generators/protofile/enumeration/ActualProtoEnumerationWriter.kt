package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.enumeration

import com.squareup.kotlinpoet.KModifier

object ActualProtoEnumerationWriter : ProtoEnumerationWriter(isActual = true) {
    override val modifiers: List<KModifier> = listOf(KModifier.ACTUAL)
}