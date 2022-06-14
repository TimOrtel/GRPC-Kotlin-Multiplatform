package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asTypeName
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.Types

/**
 * @property hasDefaultValue if the value is always set or if it can be null
 * @property protoId the unique integer index for this attribute
 */
class ProtoMessageAttribute(
    val name: String,
    val commonType: ClassName,
    val types: Types,
    val attributeType: AttributeType,
    val protoId: Int
) {
    val capitalizedName = name.capitalize()
}

sealed class AttributeType(val isEnum: Boolean)

/**
 * @property inOneOf if this attribute is in a one of
 */
class Scalar(val inOneOf: Boolean, isEnum: Boolean) : AttributeType(isEnum)

class Repeated(isEnum: Boolean) : AttributeType(isEnum)

class MapType(val keyTypes: Types, val valueTypes: Types) : AttributeType(false) {
    val commonMapType = Map::class.asTypeName().parameterizedBy(keyTypes.commonType, valueTypes.commonType)
    val commonMutableMapType =
        ClassName("kotlin.collections", "MutableMap").parameterizedBy(keyTypes.commonType, valueTypes.commonType)

    val jsMapType = ClassName("kotlin.collections", "MutableMap").parameterizedBy(keyTypes.jsType, valueTypes.jsType)
}