package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.protofile.enumeration

import com.squareup.kotlinpoet.KModifier

object CommonProtoEnumerationWriter : ProtoEnumerationWriter(isActual = false) {
    override val modifiers: List<KModifier> = emptyList()
}
