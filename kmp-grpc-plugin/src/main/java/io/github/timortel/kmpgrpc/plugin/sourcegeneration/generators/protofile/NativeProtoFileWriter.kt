package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.enumeration.ActualProtoEnumerationWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.enumeration.ProtoEnumerationWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.NativeProtoMessageWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.ProtoMessageWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.service.NativeProtoServiceWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.service.ProtoServiceWriter

object NativeProtoFileWriter : ProtoFileWriter(isActual = true) {
    override val protoServiceWriter: ProtoServiceWriter = NativeProtoServiceWriter
    override val protoMessageWriter: ProtoMessageWriter = NativeProtoMessageWriter
    override val protoEnumWriter: ProtoEnumerationWriter = ActualProtoEnumerationWriter
}
