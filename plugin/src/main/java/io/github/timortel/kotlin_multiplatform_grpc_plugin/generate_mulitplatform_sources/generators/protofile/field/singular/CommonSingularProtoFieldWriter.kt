package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.field.singular

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.field.ProtoMessageField

object CommonSingularProtoFieldWriter : SingularProtoFieldWriter() {
    override val attrs: List<KModifier> = emptyList()

    override fun PropertySpec.Builder.modifyProperty(field: ProtoMessageField) = Unit

    override fun PropertySpec.Builder.modifyIsSetProperty(field: ProtoMessageField) = Unit
}
