package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.field.repeated

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.field.ProtoMessageField

object CommonRepeatedProtoFieldWriter : RepeatedProtoFieldWriter() {
    override val attrs: List<KModifier> = emptyList()

    override fun modifyListProperty(builder: PropertySpec.Builder, field: ProtoMessageField) = Unit

    override fun modifyCountProperty(builder: PropertySpec.Builder, field: ProtoMessageField) = Unit
}
