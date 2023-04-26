package io.github.timortel.kotlin_multiplatform_grpc_lib.message

import com.google.protobuf.WireFormat.FieldType

/**
 * On the JVM, our [DataType] is identical to the [FieldType].
 */
actual typealias DataType = FieldType