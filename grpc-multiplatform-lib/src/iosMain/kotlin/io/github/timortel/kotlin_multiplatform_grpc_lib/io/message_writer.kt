package io.github.timortel.kotlin_multiplatform_grpc_lib.io

import cocoapods.Protobuf.GPBCodedOutputStream
import cocoapods.Protobuf.GPBLogicalRightShift32
import cocoapods.Protobuf.GPBWireFormatLengthDelimited
import cocoapods.Protobuf.GPBWireFormatMakeTag
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.KMMessage
import platform.posix.int32_t
import platform.posix.uint8_t

fun writeKMMessage(stream: GPBCodedOutputStream, fieldNumber: Int, msg: KMMessage) {
    GPBWriteRawVarint32(stream, GPBWireFormatMakeTag(fieldNumber.toUInt(), GPBWireFormatLengthDelimited).toInt())
    GPBWriteRawVarint32(stream, msg.requiredSize.toInt())

    msg.serialize(stream)
}

/**
 * A copy from GPBCodedOutputStream
 */
private fun GPBWriteRawVarint32(stream: GPBCodedOutputStream, value: int32_t) {
    @Suppress("NAME_SHADOWING")
    var value = value
    while(true) {
        if ((value and 0x7F) == 0) {
            val v: uint8_t = value.toUByte();
            stream.writeRawByte(v);
            return;
        } else {
            stream.writeRawByte(((value and 0x7F) or 0x80).toUByte())
            value = GPBLogicalRightShift32(value, 7);
        }
    }
}