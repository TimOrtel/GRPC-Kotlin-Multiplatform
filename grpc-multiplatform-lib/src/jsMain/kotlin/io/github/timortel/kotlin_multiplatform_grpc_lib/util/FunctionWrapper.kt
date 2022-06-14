package io.github.timortel.kotlin_multiplatform_grpc_lib.util

import io.github.timortel.kotlin_multiplatform_grpc_lib.JSPB

fun <K, L, J> ((JSPB.BinaryWriter, K, L) -> J).wrap(writer: JSPB.BinaryWriter): dynamic {
    val f = this

    return { a: K, b: L -> f(writer, a, b) }
}

fun <K, L, J, F> ((JSPB.BinaryWriter, K, L, J) -> F).wrap(writer: JSPB.BinaryWriter): dynamic {
    val f = this

    return { a: K, b: L, c: J -> f(writer, a, b, c) }
}

fun <K> ((JSPB.BinaryReader) -> K).wrap(reader: JSPB.BinaryReader): dynamic {
    val f = this
    return { f(reader) }
}

fun <K, J, L> ((JSPB.BinaryReader, K, J) -> L).wrap(reader: JSPB.BinaryReader): dynamic {
    val f = this
    return { a: K, b: J -> f(reader, a, b) }
}
