package io.github.timortel.kotlin_multiplatform_grpc_lib.io

import cocoapods.Protobuf.GPBCodedInputStream

/**
 * Additional wrapper of [GPBCodedInputStream] to keep track of the [recursionDepth].
 */
data class GPBCodedInputStreamWrapper(val stream: GPBCodedInputStream, var recursionDepth: Int = 0)