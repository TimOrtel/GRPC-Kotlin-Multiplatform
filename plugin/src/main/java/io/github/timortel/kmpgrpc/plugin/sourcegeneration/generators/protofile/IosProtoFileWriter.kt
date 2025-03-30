package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.enumeration.ActualProtoEnumerationWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.enumeration.ProtoEnumerationWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.IosProtoMessageWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.ProtoMessageWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.service.IosProtoServiceWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.service.ProtoServiceWriter

object IosProtoFileWriter : ProtoFileWriter(isActual = true) {
    override val protoServiceWriter: ProtoServiceWriter = IosProtoServiceWriter
    override val protoMessageWriter: ProtoMessageWriter = IosProtoMessageWriter
    override val protoEnumWriter: ProtoEnumerationWriter = ActualProtoEnumerationWriter
}
