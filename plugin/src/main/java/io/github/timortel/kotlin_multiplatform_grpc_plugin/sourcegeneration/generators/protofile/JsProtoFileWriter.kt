package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.protofile

import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.protofile.enumeration.ActualProtoEnumerationWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.protofile.enumeration.ProtoEnumerationWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.protofile.message.JsProtoMessageWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.protofile.message.ProtoMessageWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.service.JsProtoServiceWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.generators.service.ProtoServiceWriter

object JsProtoFileWriter : ProtoFileWriter(isActual = true) {
    override val protoServiceWriter: ProtoServiceWriter = JsProtoServiceWriter
    override val protoMessageWriter: ProtoMessageWriter = JsProtoMessageWriter
    override val protoEnumWriter: ProtoEnumerationWriter = ActualProtoEnumerationWriter
}
