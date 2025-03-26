package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.message

import com.squareup.kotlinpoet.TypeSpec
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.enumeration.CommonProtoEnumerationWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.enumeration.ProtoEnumerationWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.field.CommonProtoFieldWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.field.ProtoFieldWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.oneof.CommonProtoOneOfWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.oneof.ProtoOneOfWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.service.CommonProtoServiceWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.service.ProtoServiceWriter

/**
 * File writer that writes Kotlin Multiplatform Proto Files. These are platform independent.
 */
object CommonProtoMessageWriter : ProtoMessageWriter(false) {

    override val protoFieldWriter: ProtoFieldWriter = CommonProtoFieldWriter
    override val protoOneOfWriter: ProtoOneOfWriter = CommonProtoOneOfWriter
    override val protoEnumerationWriter: ProtoEnumerationWriter = CommonProtoEnumerationWriter
    override val protoServiceWriter: ProtoServiceWriter = CommonProtoServiceWriter

    override fun applyToClass(
        builder: TypeSpec.Builder,
        message: io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.ProtoMessage
    ) = Unit
}
