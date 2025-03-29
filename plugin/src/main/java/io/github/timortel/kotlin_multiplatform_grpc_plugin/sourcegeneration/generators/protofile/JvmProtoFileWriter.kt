package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.protofile

import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.protofile.enumeration.ActualProtoEnumerationWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.protofile.enumeration.ProtoEnumerationWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.protofile.message.JvmProtoMessageWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.protofile.message.ProtoMessageWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.service.JvmProtoServiceWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.service.ProtoServiceWriter

object JvmProtoFileWriter : ProtoFileWriter(isActual = true) {
    override val protoServiceWriter: ProtoServiceWriter = JvmProtoServiceWriter
    override val protoMessageWriter: ProtoMessageWriter = JvmProtoMessageWriter
    override val protoEnumWriter: ProtoEnumerationWriter = ActualProtoEnumerationWriter
}
