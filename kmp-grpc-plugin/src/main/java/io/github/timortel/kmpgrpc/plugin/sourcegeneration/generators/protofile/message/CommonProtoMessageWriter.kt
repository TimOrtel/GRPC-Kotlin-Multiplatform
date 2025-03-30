package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message

import com.squareup.kotlinpoet.TypeSpec
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.enumeration.CommonProtoEnumerationWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.enumeration.ProtoEnumerationWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.CommonProtoFieldWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.ProtoFieldWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.oneof.CommonProtoOneOfWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.oneof.ProtoOneOfWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.service.CommonProtoServiceWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.service.ProtoServiceWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage

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
        message: ProtoMessage
    ) = Unit
}
