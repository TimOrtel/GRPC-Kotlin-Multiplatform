package io.github.timortel.kotlin_multiplatform_grpc_lib.io

import org.khronos.webgl.Uint8Array

fun Uint8Array.toByteArray(): ByteArray = unsafeCast<ByteArray>()