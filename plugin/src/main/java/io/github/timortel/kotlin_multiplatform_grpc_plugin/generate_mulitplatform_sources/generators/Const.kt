package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.MemberName.Companion.member
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.kmTimeUnit
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.field.ProtoFieldCardinality

object Const {
    object Service {

        const val CHANNEL_PROPERTY_NAME = "channel"
        const val CALL_OPTIONS_PROPERTY_NAME = "callOptions"

        fun getName(service: ProtoService): String = "KM${service.serviceName.capitalize()}Stub"

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

        object JVM {
            const val PROPERTY_CHANNEL = "channel"
            const val PROPERTY_CALL_OPTIONS = "callOptions"

            fun nativeServiceClassName(protoFile: ProtoFile, service: ProtoService): ClassName =
                ClassName(
                    protoFile.pkg,
                    service.serviceName.capitalize() + "GrpcKt",
                    service.serviceName.capitalize() + "CoroutineStub"
                )

            object Companion {
                fun methodDescriptorPropertyName(service: ProtoService, rpc: ProtoRpc): String {
                    return "methodDescriptor${rpc.rpcName.capitalize()}"
                }
            }
        }

        object JS {
            /**
             * Service name of the js-kotlin bridge class
             */
            fun jsServiceName(service: ProtoService): String = "JS_" + service.serviceName.capitalize()

            fun nativeServiceClassName(protoFile: ProtoFile, service: ProtoService) =
                ClassName(protoFile.pkg, jsServiceName(service))
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
            fun parentSealedClassName(message: ProtoMessage, oneOf: ProtoOneOf) =
                message.commonType.nestedClass(oneOf.capitalizedName)

            fun childClassName(message: ProtoMessage, oneOf: ProtoOneOf, attr: ProtoMessageField) =
                parentSealedClassName(message, oneOf).nestedClass(attr.capitalizedName)

            fun unknownClassName(message: ProtoMessage, oneOf: ProtoOneOf) = parentSealedClassName(message, oneOf).nestedClass("Unknown")
            fun notSetClassName(message: ProtoMessage, oneOf: ProtoOneOf) = parentSealedClassName(message, oneOf).nestedClass("NotSet")

            fun propertyName(oneOf: io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.ProtoOneOf) = oneOf.name

            const val REQUIRED_SIZE_PROPERTY_NAME = "requiredSize"

            const val SERIALIZE_FUNCTION_NAME = "serialize"
            const val SERIALIZE_FUNCTION_STREAM_PARAM_NAME = "stream"
        }

        object Attribute {
            /**
             * @return the property name of the given attribute in the generated kotlin file for the message.
             */
            fun propertyName(field: io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.field.ProtoMessageField): String {
                return when (field.cardinality) {
                    ProtoFieldCardinality.Implicit, ProtoFieldCardinality.Optional -> field.name
                    ProtoFieldCardinality.Repeated -> Repeated.listPropertyName(field)
                }
            }

            object Scalar {
                object JVM {
                    fun getFunction(message: ProtoMessage, attr: ProtoMessageField) =
                        message.jvmType.member(attr.name)

                    fun setFunction(message: ProtoMessage, attr: ProtoMessageField) =
                        message.jvmType.member("set${attr.capitalizedName}")

                    fun setEnumValueFunction(message: ProtoMessage, attr: ProtoMessageField) =
                        message.jvmType.member("set${attr.capitalizedName}Value")

                    fun getHasFunction(message: ProtoMessage, attr: ProtoMessageField) =
                        message.jvmType.member("has${attr.capitalizedName}")
                }

                object JS {
                    fun getFunction(message: ProtoMessage, attr: ProtoMessageField) =
                        message.jsType.member("get${attr.name.lowercase().capitalize()}")

                    fun getHasFunction(message: ProtoMessage, attr: ProtoMessageField) =
                        message.jsType.member("has${attr.name.lowercase().capitalize()}")

                    fun setFunction(message: ProtoMessage, attr: ProtoMessageField) =
                        message.jsType.member("set${attr.name.lowercase().capitalize()}")
                }

                object IosJvm {
                    fun isMessageSetFunctionName(message: ProtoMessage, attr: ProtoMessageField): String {
                        var name = "is${attr.capitalizedName}Set"
                        val attrNames = message.attributes.map { it.name }

                        while (name in attrNames) {
                            name = "_$name"
                        }

                        return name
                    }
                }
            }

            object Repeated {

                fun listPropertyName(attr: io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.field.ProtoMessageField) = "${attr.name}List"

                object JS {
                    fun setListFunctionName(attr: ProtoMessageField): String =
                        "set${attr.name.lowercase().capitalize()}List"

                    fun getListFunctionName(attr: ProtoMessageField): String =
                        "get${attr.name.lowercase().capitalize()}List"

                    fun clearListFunctionName(attr: ProtoMessageField): String =
                        "clear${attr.name.lowercase().capitalize()}List"
                }

                object JVM {
                    fun getListFunction(message: ProtoMessage, attr: ProtoMessageField) =
                        message.jvmType.member("${attr.name}List")

                    fun addAllFunction(message: ProtoMessage, attr: ProtoMessageField) =
                        message.jvmType.member("addAll${attr.capitalizedName}")

                    fun addAllValuesFunction(message: ProtoMessage, attr: ProtoMessageField) =
                        message.jvmType.member("addAll${attr.capitalizedName}Value")
                }
            }

            object Enum {

                object JVM {
                    fun getValueFunction(message: ProtoMessage, attr: ProtoMessageField) =
                        message.jvmType.member("${attr.name}Value")

                    fun getValueListFunction(message: ProtoMessage, attr: ProtoMessageField) =
                        message.jvmType.member("${attr.name}ValueList()")
                }

                object JS {
                    fun getValueFunction(message: ProtoMessage, attr: ProtoMessageField) =
                        message.jsType.member("get${attr.name.lowercase().capitalize()}")

                    fun getValueListFunction(message: ProtoMessage, attr: ProtoMessageField) =
                        message.jsType.member("get${attr.name.lowercase().capitalize()}List")
                }
            }

            object Map {
                fun propertyName(attr: io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.field.ProtoMapField): String = "${attr.name}Map"

                object JVM {
                    fun propertyName(attr: ProtoMessageField): String = "${attr.name}Map"

                    fun putAllFunctionName(attr: ProtoMessageField): String = "putAll${attr.capitalizedName}"
                }

                object JS {
                    fun getMapFunctionName(attr: ProtoMessageField): String =
                        "get${attr.name.lowercase().capitalize()}Map"
                }
            }
        }

        object Constructor {
            object JVM {
                const val PARAM_IMPL = "impl"
            }

            object JS {
                const val PARAM_IMPL = "jsImpl"
            }
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
        const val buildFunctionName: String = "build"

        object Attribute {
            object Scalar {
                fun propertyName(attr: ProtoMessageField): String = attr.name
            }

            object Repeated {
                fun propertyName(attr: ProtoMessageField): String = "${attr.name}List"
            }

            object Map {
                fun propertyName(attr: ProtoMessageField): String = "${attr.name}Map"
            }
        }

        object OneOf {
            fun propertyName(message: ProtoMessage, oneOf: ProtoOneOf): String = oneOf.name
        }
    }

    object Enum {
        const val getEnumForNumFunctionName = "getEnumForNumber"
        const val NUMBER_PROPERTY_NAME = "number"

        fun commonEnumName(protoEnum: ProtoEnum): String = "KM${protoEnum.name.capitalize()}"
        fun commonEnumName(protoEnumName: String): String = "KM${protoEnumName.capitalize()}"

        fun getEnumForNumFunction(protoEnum: ProtoEnum, pkg: String) =
            ClassName(pkg, commonEnumName(protoEnum)).member(getEnumForNumFunctionName)
    }
}