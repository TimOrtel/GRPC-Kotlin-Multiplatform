package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.constants

import com.squareup.kotlinpoet.LONG

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

                val ParamDuration = Property("duration", LONG)
                val ParamUnit = Property("unit", kmTimeUnit)
            }
        }

        object RpcCall {
            const val PARAM_REQUEST = "request"
            const val PARAM_METADATA = "metadata"
        }
    }

    object Message {
        object SerializeFunction {
            const val NAME = "serialize"
            const val STREAM_PARAM = "stream"
        }

        object OneOf {
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
            object DataDeserializationFunction {
                const val NAME = "deserialize"
                const val DATA_PARAM = "data"
            }

            object WrapperDeserializationFunction {
                const val NAME = "deserializeFromWrapper"
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