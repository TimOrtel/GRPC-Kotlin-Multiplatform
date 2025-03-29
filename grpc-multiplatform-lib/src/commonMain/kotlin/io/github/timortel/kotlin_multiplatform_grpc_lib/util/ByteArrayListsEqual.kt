package io.github.timortel.kotlin_multiplatform_grpc_lib.util

/**
 * Perform equals check on two ByteArray lists.
 */
fun byteArrayListsEqual(first: List<ByteArray>, second: List<ByteArray>): Boolean {
    if (first.size != second.size) return false
    return first.zip(second).all { (a, b) -> a.contentEquals(b) }
}
