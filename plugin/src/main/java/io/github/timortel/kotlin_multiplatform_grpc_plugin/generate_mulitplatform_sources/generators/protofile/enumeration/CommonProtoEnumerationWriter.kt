package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.enumeration

import com.squareup.kotlinpoet.KModifier

object CommonProtoEnumerationWriter : ProtoEnumerationWriter(isActual = false) {
    override val modifiers: List<KModifier> = emptyList()
}
