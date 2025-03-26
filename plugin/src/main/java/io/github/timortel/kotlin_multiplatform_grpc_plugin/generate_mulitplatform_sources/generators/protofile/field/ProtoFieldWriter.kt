package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.field

import com.squareup.kotlinpoet.TypeSpec
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.field.map.ProtoMapFieldWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.field.repeated.RepeatedProtoFieldWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.field.singular.SingularProtoFieldWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.field.ProtoFieldCardinality
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.field.ProtoMapField
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.field.ProtoMessageField

abstract class ProtoFieldWriter {

    abstract val singularProtoFieldWriter: SingularProtoFieldWriter
    abstract val repeatedProtoFieldWriter: RepeatedProtoFieldWriter
    abstract val mapProtoFieldWriter: ProtoMapFieldWriter


    fun addMessageField(builder: TypeSpec.Builder, field: ProtoMessageField) {
        when (field.cardinality) {
            is ProtoFieldCardinality.Singular -> singularProtoFieldWriter.addField(builder, field, field.cardinality)
            ProtoFieldCardinality.Repeated -> repeatedProtoFieldWriter.addField(builder, field)
        }
    }

    fun addMapField(builder: TypeSpec.Builder, field: ProtoMapField) {
        mapProtoFieldWriter.addMapField(builder, field)
    }
}
