package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.enumeration.CommonProtoEnumerationWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.enumeration.ProtoEnumerationWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.CommonProtoMessageWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.ProtoMessageWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.service.CommonProtoServiceWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.service.ProtoServiceWriter

object CommonProtoFileWriter : ProtoFileWriter(isActual = false) {
    override val protoServiceWriter: ProtoServiceWriter = CommonProtoServiceWriter
    override val protoMessageWriter: ProtoMessageWriter = CommonProtoMessageWriter
    override val protoEnumWriter: ProtoEnumerationWriter = CommonProtoEnumerationWriter
}
