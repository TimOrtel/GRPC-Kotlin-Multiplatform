package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources

import com.squareup.kotlinpoet.ClassName

/**
 * @property isEnum if this type is a proto enum type
 * @property isNullable if this type can be null.
 */
data class Types(
    val commonType: ClassName,
    val jvmType: ClassName,
    val jsType: ClassName,
    val iosType: ClassName,
    val doDiffer: Boolean,
    val isEnum: Boolean,
    val isNullable: Boolean,
    val protoType: ProtoType
)

enum class ProtoType {
    DOUBLE,
    FLOAT,
    INT_32,
    INT_64,
    BOOL,
    STRING,
    MAP,
    MESSAGE,
    ENUM
}