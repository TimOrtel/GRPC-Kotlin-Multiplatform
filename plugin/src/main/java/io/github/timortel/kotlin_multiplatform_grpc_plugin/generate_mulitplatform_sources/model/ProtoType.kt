package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model

sealed interface ProtoType {
    sealed interface MapKeyType : ProtoType

    data object DoubleType : ProtoType
    data object FloatType : ProtoType
    data object Int32Type : ProtoType, MapKeyType
    data object Int64Type : ProtoType, MapKeyType
    data object UInt32Type : ProtoType, MapKeyType
    data object UInt64Type : ProtoType, MapKeyType
    data object SInt32Type : ProtoType, MapKeyType
    data object SInt64Type : ProtoType, MapKeyType
    data object Fixed32Type : ProtoType, MapKeyType
    data object Fixed64Type : ProtoType, MapKeyType
    data object SFixed32Type : ProtoType, MapKeyType
    data object SFixed64Type : ProtoType, MapKeyType
    data object BoolType : ProtoType, MapKeyType
    data object StringType : ProtoType, MapKeyType
    data object BytesType : ProtoType

    /**
     * Message or Enum Types
     */
    data class DefType(val declaration: String) : ProtoType
}