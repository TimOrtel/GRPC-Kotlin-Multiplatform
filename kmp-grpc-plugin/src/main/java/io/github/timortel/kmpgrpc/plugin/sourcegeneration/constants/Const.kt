package io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants

import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.asTypeName
import kotlin.time.Duration

object Const {
    object Service {

        const val CHANNEL_PROPERTY_NAME = "channel"
        const val CALL_OPTIONS_PROPERTY_NAME = "callOptions"

        object Constructor {
            const val CHANNEL_PARAMETER_NAME = "channel"
        }

        object Functions {
            object WithDeadlineAfter {
                const val NAME = "withDeadlineAfter"

                val ParamDuration = Property("duration", Duration::class.asTypeName())
            }
        }

        object RpcCall {
            const val PARAM_REQUEST = "request"
            const val PARAM_REQUESTS = "requests"
            const val PARAM_METADATA = "metadata"
        }
    }

    object Message {
        val reservedAttributeNames = setOf("fullName", "requiredSize", Constructor.UnknownFields.name)

        val fullNameProperty = Property("fullName", STRING)

        object Constructor {
            val UnknownFields = Property("unknownFields", LIST.parameterizedBy(unknownField))
        }

        object SerializeFunction {
            const val NAME = "serialize"
            const val STREAM_PARAM = "stream"
        }

        object OneOf {
            val reservedAttributeNames = setOf("requiredSize")

            const val REQUIRED_SIZE_PROPERTY_NAME = "requiredSize"

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
        }

        object Companion {
            val fullNameProperty = Property("fullName", STRING)

            object WrapperDeserializationFunction {
                const val NAME = "deserialize"
                const val STREAM_PARAM = "stream"
            }
        }
    }

    object DSL {
        const val BUILD_FUNCTION_NAME: String = "build"
    }

    object Enum {
        const val GET_ENUM_FOR_FUNCTION_NAME = "getEnumForNumber"
        const val NUMBER_PROPERTY_NAME = "number"
    }
}