package io.github.timortel.kmpgrpc.shared

/**
 * The supported data types that can be used as message fields.
 */
enum class DataType {
    DOUBLE,
    FLOAT,
    INT64,
    UINT64,
    INT32,
    FIXED64,
    FIXED32,
    BOOL,
    STRING,
    GROUP,
    MESSAGE,
    BYTES,
    UINT32,
    ENUM,
    SFIXED32,
    SFIXED64,
    SINT32,
    SINT64
}