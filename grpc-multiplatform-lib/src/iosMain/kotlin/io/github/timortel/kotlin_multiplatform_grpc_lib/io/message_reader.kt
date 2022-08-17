package io.github.timortel.kotlin_multiplatform_grpc_lib.io

import cocoapods.Protobuf.*
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.KMMessage

fun <M : KMMessage> readKMMessage(wrapper: GPBCodedInputStreamWrapper, messageFactory: (GPBCodedInputStreamWrapper) -> M): M {
    checkRecursionLimit(wrapper)
    val length = wrapper.stream.readInt32()
    val oldLimit = wrapper.stream.pushLimit(length.toULong())
    wrapper.recursionDepth++
    val out = messageFactory(wrapper)
    wrapper.stream.checkLastTagWas(0)
    wrapper.recursionDepth--
    wrapper.stream.popLimit(oldLimit)

    return out
}

private fun checkRecursionLimit(wrapper: GPBCodedInputStreamWrapper) {
    if(wrapper.recursionDepth >= 100) {
        throw RuntimeException("Recursion depth exceeded.")
    }
}