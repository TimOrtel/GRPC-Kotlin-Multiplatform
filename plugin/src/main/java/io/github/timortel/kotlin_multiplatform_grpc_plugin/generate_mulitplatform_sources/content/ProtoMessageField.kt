package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asTypeName
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.ProtoType
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.Types
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import java.util.*

/**
 * @property hasDefaultValue if the value is always set or if it can be null
 * @property protoId the unique integer index for this attribute
 */
class ProtoMessageField(
    val name: String,
    val commonType: ClassName,
    val types: Types,
    val fieldCardinality: FieldCardinality,
    val protoId: Int,
    val isOneOfAttribute: Boolean
) {
    val capitalizedName = name.capitalize(Locale.ROOT)

    /**
     * The default value for this attribute
     * @param useEmptyMessage if an empty message should be used, or null
     */
    fun commonDefaultValue(mutable: Boolean, useEmptyMessage: Boolean): CodeBlock = when (fieldCardinality) {
        is Scalar -> when (types.protoType) {
            ProtoType.DOUBLE -> CodeBlock.of("0.0")
            ProtoType.FLOAT -> CodeBlock.of("0f")
            ProtoType.INT_32 -> CodeBlock.of("0")
            ProtoType.INT_64 -> CodeBlock.of("0L")
            ProtoType.BOOL -> CodeBlock.of("false")
            ProtoType.STRING -> CodeBlock.of("\"\"")
            ProtoType.MESSAGE -> if (useEmptyMessage) {
                CodeBlock.of("%T()", types.commonType)
            } else CodeBlock.of("null")
            ProtoType.ENUM -> {
                CodeBlock.of(
                    "%T.%N(0)",
                    types.commonType,
                    Const.Enum.getEnumForNumFunctionName
                )
            }

            ProtoType.MAP -> throw IllegalStateException()
        }

        is Repeated -> CodeBlock.of(
            "%M()",
            MemberName("kotlin.collections", if (mutable) "mutableListOf" else "emptyList")
        )

        is MapType -> CodeBlock.of(
            "%M()",
            MemberName("kotlin.collections", if (mutable) "mutableMapOf" else "emptyMap")
        )
    }
}

sealed class FieldCardinality(val isEnum: Boolean)

/**
 * @property inOneOf if this attribute is in a one of
 */
class Scalar(val inOneOf: Boolean, val type: Type, isEnum: Boolean) : FieldCardinality(isEnum) {
    enum class Type {
        IMPLICIT,
        OPTIONAL
    }
}

class Repeated(isEnum: Boolean) : FieldCardinality(isEnum)

class MapType(val keyTypes: Types, val valueTypes: Types) : FieldCardinality(false) {
    val commonMapType = Map::class.asTypeName().parameterizedBy(keyTypes.commonType, valueTypes.commonType)
    val commonMutableMapType =
        ClassName("kotlin.collections", "MutableMap").parameterizedBy(keyTypes.commonType, valueTypes.commonType)

    val jsMapType = ClassName("kotlin.collections", "MutableMap").parameterizedBy(keyTypes.jsType, valueTypes.jsType)
}