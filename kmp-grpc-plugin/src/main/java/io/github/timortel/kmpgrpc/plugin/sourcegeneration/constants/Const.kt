package io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.asTypeName
import kotlin.time.Duration

object Const {

    /**
     * Maximum value of a field number
     */
    const val FIELD_NUMBER_MAX_VALUE = 536870911

    object Service {

        const val CHANNEL_PROPERTY_NAME = "channel"
        const val CALL_OPTIONS_PROPERTY_NAME = "callOptions"

        object Constructor {
            const val CHANNEL_PARAMETER_NAME = "channel"
        }

        object Functions {
            object WithDeadlineAfter {
                const val NAME = "withDeadlineAfter"

                val ParamDuration = Property.of("duration", Duration::class.asTypeName())
            }
        }

        object RpcCall {
            const val PARAM_REQUEST = "request"
            const val PARAM_REQUESTS = "requests"
            const val PARAM_METADATA = "metadata"
        }
    }

    object Message {
        val fullNameProperty = Property.of("fullName", STRING)

        val isInitializedProperty = Property.of("isInitialized", BOOLEAN)

        object Constructor {
            val UnknownFields = Property.of("unknownFields", LIST.parameterizedBy(unknownField))
            val MessageExtensions = Property.of("extensions", kmMessageExtensions)
        }

        object SerializeFunction {
            const val NAME = "serialize"
            const val STREAM_PARAM = "stream"
        }

        object OneOf {
            val reservedAttributeNames = setOf("requiredSize")

            const val REQUIRED_SIZE_PROPERTY_NAME = "requiredSize"
            val isInitializedProperty = Property.of("isInitialized", BOOLEAN)

            const val SERIALIZE_FUNCTION_NAME = "serialize"
            const val SERIALIZE_FUNCTION_STREAM_PARAM_NAME = "stream"
        }

        object BasicFunctions {
            object EqualsFunction {
                const val NAME = "equals"
                const val OTHER_PARAM = "other"
            }

            object HashCodeFunction {
                const val NAME = "hashCode"
            }

            object CopyFunction {
                const val NAME = "copy"
            }

            object ToStringFunction {
                const val NAME = "toString"
            }
        }

        object Companion {
            val fullNameProperty = Property.of("fullName", STRING)
            val defaultExtensionRegistryProperty = Property.of("defaultExtensionRegistry", kmExtensionRegistry)

            object WrapperDeserializationFunction {
                const val NAME = "deserialize"

                val STREAM_PARAM = Property.of("stream", CodedInputStream)
                val EXTENSION_REGISTRY_PARAM = Property.of("extensionRegistry", kmExtensionRegistry)

                const val TAG_LOCAL_VARIABLE = "tag_"
                const val ENUM_NUMBER_VALUE_LOCAL_VARIABLE = "enumNumberValue_"
                const val ENUM_VALUE_LOCAL_VARIABLE = "enumValue_"
                const val UNKNOWN_FIELDS_LOCAL_VARIABLE = "unknownFields"
                const val EXTENSION_BUILDER_LOCAL_VARIABLE = "extensionBuilder"
            }
        }

        val reservedAttributeNames = setOf(
            fullNameProperty.name,
            "requiredSize",
            isInitializedProperty.name,
            Companion.WrapperDeserializationFunction.TAG_LOCAL_VARIABLE,
            Companion.WrapperDeserializationFunction.ENUM_NUMBER_VALUE_LOCAL_VARIABLE,
            Companion.WrapperDeserializationFunction.ENUM_VALUE_LOCAL_VARIABLE,
            Constructor.UnknownFields.name
        )
    }

    object DSL {
        const val BUILD_FUNCTION_NAME: String = "build"
        val MessageExtensions = Property.of("extensions", kmExtensionBuilder)
    }

    object Enum {
        const val GET_ENUM_FOR_FUNCTION_NAME = "getEnumForNumber"
        const val GET_ENUM_FOR_OR_NULL_FUNCTION_NAME = "getEnumForNumberOrNull"
        const val NUMBER_PROPERTY_NAME = "number"
    }
}
