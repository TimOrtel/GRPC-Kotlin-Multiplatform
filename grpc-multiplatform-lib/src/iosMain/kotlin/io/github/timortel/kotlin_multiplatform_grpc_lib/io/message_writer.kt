package io.github.timortel.kotlin_multiplatform_grpc_lib.io

import cocoapods.Protobuf.GPBCodedOutputStream
import cocoapods.Protobuf.GPBLogicalRightShift32
import cocoapods.Protobuf.GPBWireFormatLengthDelimited
import cocoapods.Protobuf.GPBWireFormatMakeTag
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.KMMessage
import platform.posix.int32_t
import platform.posix.uint8_t

fun writeKMMessage(stream: GPBCodedOutputStream, fieldNumber: Int, msg: KMMessage) {
    stream.writeUInt32NoTag(GPBWireFormatMakeTag(fieldNumber.toUInt(), GPBWireFormatLengthDelimited))
    stream.writeUInt32NoTag(msg.requiredSize.toUInt())

    msg.serialize(stream)
}