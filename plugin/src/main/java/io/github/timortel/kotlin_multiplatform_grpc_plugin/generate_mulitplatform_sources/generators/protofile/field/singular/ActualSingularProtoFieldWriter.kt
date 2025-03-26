package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.field.singular

import com.squareup.kotlinpoet.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.ProtoType
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.field.ProtoMessageField

object ActualSingularProtoFieldWriter : SingularProtoFieldWriter() {

    override val attrs: List<KModifier> = listOf(KModifier.ACTUAL)

    override fun PropertySpec.Builder.modifyProperty(field: ProtoMessageField) {
        if (field.type is ProtoType.DefType && field.type.isMessage) {
            initializer("%N ?: %T()", field.name, field.type.resolve())
        } else {
            initializer(field.name)
        }
    }

    override fun PropertySpec.Builder.modifyIsSetProperty(field: ProtoMessageField) {
        initializer("%N != null", field.name)
    }
}
