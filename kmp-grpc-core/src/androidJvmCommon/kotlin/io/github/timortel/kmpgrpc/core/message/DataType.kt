package io.github.timortel.kmpgrpc.core.message

import com.google.protobuf.WireFormat.FieldType

/**
 * On the JVM, our [DataType] is identical to the [FieldType].
 */
actual typealias DataType = FieldType