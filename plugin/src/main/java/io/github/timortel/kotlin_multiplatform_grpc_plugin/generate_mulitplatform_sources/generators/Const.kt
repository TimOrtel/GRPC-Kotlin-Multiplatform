package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.MemberName.Companion.member
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.*

object Const {
    object Service {

        fun getName(service: ProtoService): String = "KM${service.serviceName.capitalize()}Stub"

        object JVM {
            const val PROPERTY_JVM_IMPL = "impl"

            fun nativeServiceClassName(protoFile: ProtoFile, service: ProtoService): ClassName =
                ClassName(
                    protoFile.pkg,
                    service.serviceName.capitalize() + "GrpcKt",
                    service.serviceName.capitalize() + "CoroutineStub"
                )
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
        object CommonFunction {
            const val NAME = "common"
            const val PARAMETER_NATIVE = "native"

            object JVM {
                fun commonFunction(attr: ProtoMessageAttribute): MemberName = commonFunction(attr.types.jvmType)

                fun commonFunction(jvmType: ClassName) = MemberName(jvmType.packageName, NAME)
            }

            object JS {
                fun commonFunction(attr: ProtoMessageAttribute): MemberName = commonFunction(attr.types.jsType)

                fun commonFunction(jsType: ClassName) = MemberName(jsType.packageName, NAME)
            }
        }

        object OneOf {
            fun propertyCaseName(oneOf: ProtoOneOf) = "${oneOf.name}Case"

            object CaseEnum {
                fun oneOfCaseClassName(oneOf: ProtoOneOf): String = oneOfCaseClassName(oneOf.name)
                fun oneOfCaseClassName(enumName: String): String = "KM${enumName.capitalize()}Case"

                object EnumNotSet {
                    fun name(oneOf: ProtoOneOf): String = "${oneOf.name.uppercase()}_NOT_SET"
                }

                object EnumField {
                    fun name(attr: ProtoMessageAttribute): String = attr.name.uppercase()
                }
            }

            object JS {
                fun getCaseFunctionName(oneOf: ProtoOneOf): String = "get${oneOf.name.lowercase().capitalize()}Case"
            }
        }

        object Attribute {
            object Scalar {
                object JVM {
                    fun getFunction(message: ProtoMessage, attr: ProtoMessageAttribute) =
                        message.jvmType.member(attr.name)

                    fun setFunction(message: ProtoMessage, attr: ProtoMessageAttribute) =
                        message.jvmType.member("set${attr.capitalizedName}")

                    fun setEnumValueFunction(message: ProtoMessage, attr: ProtoMessageAttribute) =
                        message.jvmType.member("set${attr.capitalizedName}Value")

                    fun getHasFunction(message: ProtoMessage, attr: ProtoMessageAttribute) =
                        message.jvmType.member("has${attr.capitalizedName}")
                }

                object JS {
                    fun getFunction(message: ProtoMessage, attr: ProtoMessageAttribute) =
                        message.jsType.member("get${attr.name.lowercase().capitalize()}")

                    fun getHasFunction(message: ProtoMessage, attr: ProtoMessageAttribute) =
                        message.jsType.member("has${attr.name.lowercase().capitalize()}")

                    fun setFunction(message: ProtoMessage, attr: ProtoMessageAttribute) =
                        message.jsType.member("set${attr.name.lowercase().capitalize()}")
                }

                object IOS {
                    fun isMessageSetFunctionName(message: ProtoMessage, attr: ProtoMessageAttribute): String {
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
                fun addAllFunctionName(attr: ProtoMessageAttribute) = "addAll${attr.name.capitalize()}"

                fun listPropertyName(attr: ProtoMessageAttribute) = "${attr.name}List"

                fun countPropertyName(attr: ProtoMessageAttribute) = "${attr.name}Count"

                fun clearListFunctionName(attr: ProtoMessageAttribute) = "clear${attr.name.capitalize()}"

                object JS {
                    fun setListFunctionName(attr: ProtoMessageAttribute): String =
                        "set${attr.name.lowercase().capitalize()}List"

                    fun getListFunctionName(attr: ProtoMessageAttribute): String =
                        "get${attr.name.lowercase().capitalize()}List"

                    fun clearListFunctionName(attr: ProtoMessageAttribute): String =
                        "clear${attr.name.lowercase().capitalize()}List"
                }

                object JVM {
                    fun getListFunction(message: ProtoMessage, attr: ProtoMessageAttribute) =
                        message.jvmType.member("${attr.name}List")

                    fun addAllFunction(message: ProtoMessage, attr: ProtoMessageAttribute) =
                        message.jvmType.member("addAll${attr.capitalizedName}")

                    fun addAllValuesFunction(message: ProtoMessage, attr: ProtoMessageAttribute) =
                        message.jvmType.member("addAll${attr.capitalizedName}Value")
                }
            }

            object Enum {

                object JVM {
                    fun getValueFunction(message: ProtoMessage, attr: ProtoMessageAttribute) =
                        message.jvmType.member("${attr.name}Value")

                    fun getValueListFunction(message: ProtoMessage, attr: ProtoMessageAttribute) =
                        message.jvmType.member("${attr.name}ValueList()")
                }

                object JS {
                    fun getValueFunction(message: ProtoMessage, attr: ProtoMessageAttribute) =
                        message.jsType.member("get${attr.name.lowercase().capitalize()}")

                    fun getValueListFunction(message: ProtoMessage, attr: ProtoMessageAttribute) =
                        message.jsType.member("get${attr.name.lowercase().capitalize()}List")
                }
            }

            object Map {
                fun propertyName(attr: ProtoMessageAttribute): String = "${attr.name}Map"

                object JVM {
                    fun propertyName(attr: ProtoMessageAttribute): String = "${attr.name}Map"

                    fun putAllFunctionName(attr: ProtoMessageAttribute): String = "putAll${attr.capitalizedName}"
                }

                object JS {
                    fun getMapFunctionName(attr: ProtoMessageAttribute): String =
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

        object IOS {
            object SerializeFunction {
                const val NAME = "serialize"
                const val STREAM_PARAM = "stream"
            }
        }

        object Companion {
            object IOS {
                object DataDeserializationFunction {
                    const val NAME = "deserialize"
                    const val DATA_PARAM = "data"
                }

                object WrapperDeserializationFunction {
                    const val NAME = "deserialize"
                    const val WRAPPER_PARAM = "wrapper"
                }
            }
        }
    }

    object DSL {
        const val buildFunctionName: String = "build"

        object Attribute {
            object Scalar {
                fun attrName(attr: ProtoMessageAttribute): String = attr.name
            }

            object Repeated {
                fun attrName(attr: ProtoMessageAttribute): String = "${attr.name}List"
            }

            object Map {
                fun attrName(attr: ProtoMessageAttribute): String = "${attr.name}Map"
            }
        }
    }

    object Enum {
        const val getEnumForNumFunctionName = "getEnumForNumber"
        const val VALUE_PROPERTY_NAME = "value"

        fun commonEnumName(protoEnum: ProtoEnum): String = "KM${protoEnum.name.capitalize()}"
        fun commonEnumName(protoEnumName: String): String = "KM${protoEnumName.capitalize()}"

        fun getEnumForNumFunction(protoEnum: ProtoEnum, pkg: String) =
            ClassName(pkg, commonEnumName(protoEnum)).member(getEnumForNumFunctionName)
    }
}