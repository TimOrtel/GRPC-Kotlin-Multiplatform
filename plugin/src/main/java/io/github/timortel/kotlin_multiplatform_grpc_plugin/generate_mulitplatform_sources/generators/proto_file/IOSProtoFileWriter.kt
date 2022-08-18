package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.proto_file

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.ios.IOSDefaultAttributeValue
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.IOSMapMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.MapMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof.IOSOneOfMethodAndClassGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof.OneOfMethodAndClassGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.repeated.IOSRepeatedMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.repeated.RepeatedMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.scalar.IOSScalarMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.scalar.ScalarMessageMethodGenerator

class IOSProtoFileWriter(private val protoFile: ProtoFile) : ProtoFileWriter(protoFile, isActual = true),
    DefaultChildClassName {

    override val scalarMessageMethodGenerator: ScalarMessageMethodGenerator
        get() = IOSScalarMessageMethodGenerator
    override val repeatedMessageMethodGenerator: RepeatedMessageMethodGenerator
        get() = IOSRepeatedMessageMethodGenerator
    override val oneOfMethodAndClassGenerator: OneOfMethodAndClassGenerator
        get() = IOSOneOfMethodAndClassGenerator
    override val mapMessageMethodGenerator: MapMessageMethodGenerator
        get() = IOSMapMessageMethodGenerator

    override fun applyToClass(builder: TypeSpec.Builder, message: ProtoMessage) {
        builder.apply {
            primaryConstructor(
                FunSpec
                    .constructorBuilder()
                    .apply {
                        message.attributes.forEach { attr ->
                            if (attr.types.isNullable) {
                                addParameter(
                                    ParameterSpec.builder(attr.name, attr.commonType.copy(nullable = true))
                                        .defaultValue("null")
                                        .build()
                                )
                            } else {
                                when (attr.attributeType) {
                                    is Scalar -> {
                                        addParameter(
                                            ParameterSpec
                                                .builder(attr.name, attr.commonType)
                                                .defaultValue(IOSDefaultAttributeValue.getDefaultValueForAttr(attr))
                                                .build()
                                        )
                                    }

                                    is Repeated -> {
                                        addParameter(
                                            ParameterSpec
                                                .builder(
                                                    Const.Message.Attribute.Repeated.listPropertyName(attr),
                                                    LIST.parameterizedBy(attr.commonType)
                                                )
                                                .defaultValue("emptyList()")
                                                .build()
                                        )
                                    }

                                    is MapType -> TODO()
                                }

                            }
                        }
                    }
                    .build()
            )

            addSuperinterface(kmMessage)

            addProperty(
                PropertySpec
                    .builder("requiredSize", U_LONG, KModifier.OVERRIDE)
                    .initializer(buildRequiredSizeInitializer(message))
                    .build()
            )

            addFunction(
                FunSpec
                    .builder(Const.Message.IOS.SerializeFunction.NAME)
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter(Const.Message.IOS.SerializeFunction.STREAM_PARAM, GPBCodedOutputStream)
                    .apply { buildSerializeFunction(this, message) }
                    .build()
            )

            addFunction(
                FunSpec
                    .builder("serialize")
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(NSData)
                    .addStatement("val data = %T().apply { setLength(requiredSize) }", NSMutableData)
                    .addStatement("val stream = %T(data)", GPBCodedOutputStream)
                    .addStatement("serialize(stream)")
                    .addStatement("return data")
                    .build()
            )

            addType(
                TypeSpec
                    .companionObjectBuilder()
                    .addFunction(
                        //The function that builds the message from NSData
                        FunSpec
                            .builder(Const.Message.Companion.IOS.DataDeserializationFunction.NAME)
                            .addParameter(Const.Message.Companion.IOS.DataDeserializationFunction.DATA_PARAM, NSData)
                            .addStatement("val wrapper = %T(%T(data))", GPBCodedInputStreamWrapper, GPBCodedInputStream)
                            .addStatement(
                                "return %N(wrapper)",
                                Const.Message.Companion.IOS.WrapperDeserializationFunction.NAME
                            )
                            .returns(message.iosType)
                            .build()
                    )
                    .addFunction(
                        //The function that builds the message from a stream.
                        FunSpec
                            .builder(Const.Message.Companion.IOS.WrapperDeserializationFunction.NAME)
                            .addParameter(
                                Const.Message.Companion.IOS.WrapperDeserializationFunction.WRAPPER_PARAM,
                                GPBCodedInputStreamWrapper
                            )
                            .returns(message.iosType)
                            .apply { buildWrapperDeserializationFunction(this, message) }
                            .build()
                    )
                    .build()
            )
        }
    }

    private fun buildRequiredSizeInitializer(message: ProtoMessage): CodeBlock {
        val getComputeMember: (ProtoType, tag: Boolean) -> MemberName = { protoType, tag ->
            val name = when (protoType) {
                ProtoType.DOUBLE -> "Double"
                ProtoType.FLOAT -> "Float"
                ProtoType.INT_32 -> "Int32"
                ProtoType.INT_64 -> "Int64"
                ProtoType.BOOL -> "Bool"
                ProtoType.STRING -> "String"
                ProtoType.MESSAGE -> "Message"
                ProtoType.ENUM -> "Enum"
                else -> throw IllegalStateException()
            }
            MemberName("cocoapods.Protobuf", "GPBCompute${name}Size${if (!tag) "NoTag" else ""}")
        }

        return CodeBlock
            .builder()
            .apply {
                message.attributes.forEachIndexed { i, attr ->
                    if (i != 0) add("\n+ ")
                    when (attr.attributeType) {
                        is Scalar -> {
                            when (attr.types.protoType) {
                                ProtoType.DOUBLE,
                                ProtoType.FLOAT,
                                ProtoType.INT_32,
                                ProtoType.INT_64,
                                ProtoType.BOOL,
                                ProtoType.STRING -> {
                                    add(
                                        "%M(%L, %N)",
                                        getComputeMember(attr.types.protoType, true),
                                        attr.protoId,
                                        attr.name
                                    )
                                }

                                ProtoType.MESSAGE -> add(
                                    "%M(%L, %N)",
                                    MemberName(
                                        "io.github.timortel.kotlin_multiplatform_grpc_lib.message",
                                        "computeMessageSize"
                                    ),
                                    attr.protoId,
                                    attr.name
                                )

                                ProtoType.ENUM -> add(
                                    "%M(%L, %N.%N)",
                                    MemberName("cocoapods.Protobuf", "GPBComputeEnumSize"),
                                    attr.protoId,
                                    attr.name,
                                    Const.Enum.VALUE_PROPERTY_NAME
                                )

                                else -> throw IllegalStateException()
                            }
                        }

                        is Repeated -> {
                            val attrFieldName = Const.Message.Attribute.Repeated.listPropertyName(attr)

                            beginControlFlow("if (%N.isEmpty()) 0 else %N.let·{·e ->", attrFieldName)
                            beginControlFlow("val dataSize = e.sumOf·{")
                            when (attr.types.protoType) {
                                ProtoType.DOUBLE,
                                ProtoType.FLOAT,
                                ProtoType.INT_32,
                                ProtoType.INT_64,
                                ProtoType.BOOL,
                                ProtoType.STRING -> add("%M(it)", getComputeMember(attr.types.protoType, false))

                                ProtoType.MESSAGE -> add(
                                    "%M(it)",
                                    MemberName(
                                        "io.github.timortel.kotlin_multiplatform_grpc_lib.message",
                                        "computeMessageSizeNoTag"
                                    )
                                )

                                ProtoType.ENUM -> add(
                                    "%M(it.%N)",
                                    MemberName("cocoapods.Protobuf", "GPBComputeEnumSize"),
                                    Const.Enum.VALUE_PROPERTY_NAME
                                )

                                else -> throw IllegalStateException()
                            }
                            endControlFlow()
                            addStatement("val tagSize = %M(%L)", GPBComputeTagSize, attr.protoId)

                            val isPackable = isAttrPackable(attr)

                            if (isPackable) {
                                addStatement("dataSize + tagSize + %M(tagSize)", GPBComputeSizeTSizeAsInt32NoTag)
                            } else {
                                addStatement("dataSize + %N.size * tagSize", attrFieldName)
                            }

                            endControlFlow()
                        }

                        is MapType -> TODO()
                    }

                }
            }
            .build()
    }

    /**
     * Generate the serialize function, adds a write call for each attribute
     */
    private fun buildSerializeFunction(builder: FunSpec.Builder, message: ProtoMessage) {
        builder.apply {
            message.attributes.forEach { attr ->
                when (attr.attributeType) {
                    is Scalar -> {
                        when (attr.types.protoType) {
                            ProtoType.DOUBLE,
                            ProtoType.FLOAT,
                            ProtoType.INT_32,
                            ProtoType.INT_64,
                            ProtoType.BOOL,
                            ProtoType.STRING -> {
                                val functionName = when (attr.types.protoType) {
                                    ProtoType.DOUBLE -> "writeDouble"
                                    ProtoType.FLOAT -> "writeFloat"
                                    ProtoType.INT_32 -> "writeInt32"
                                    ProtoType.INT_64 -> "writeInt64"
                                    ProtoType.BOOL -> "writeBool"
                                    ProtoType.STRING -> "writeString"
                                    else -> throw IllegalStateException()
                                }
                                addStatement("%N.%N(%L, %N)", Const.Message.IOS.SerializeFunction.STREAM_PARAM, functionName, attr.protoId, attr.name)
                            }

                            ProtoType.MESSAGE -> {
                                beginControlFlow(
                                    "if (%N)",
                                    Const.Message.Attribute.Scalar.IOS.isMessageSetFunctionName(message, attr)
                                )

                                addStatement(
                                    "%M(%N, %L, %N)",
                                    MemberName("io.github.timortel.kotlin_multiplatform_grpc_lib.io", "writeKMMessage"),
                                    Const.Message.IOS.SerializeFunction.STREAM_PARAM,
                                    attr.protoId,
                                    attr.name
                                )

                                endControlFlow()
                            }
                            ProtoType.ENUM -> {
                                addStatement("%N.writeEnum(%L, %N.%N)", Const.Message.IOS.SerializeFunction.STREAM_PARAM, attr.name, Const.Enum.VALUE_PROPERTY_NAME)
                            }
                            ProtoType.MAP -> throw IllegalStateException()
                        }
                    }
                    is Repeated -> {

                    }
                    is MapType -> TODO()
                }
            }
        }
    }

    private fun buildWrapperDeserializationFunction(builder: FunSpec.Builder, message: ProtoMessage) {
        val getAttrVarName = { attr: ProtoMessageAttribute ->
            when (attr.attributeType) {
                is MapType -> "${attr.name}Map"
                is Repeated -> "${attr.name}List"
                is Scalar -> attr.name
            }
        }

        builder.apply {
            message.attributes.forEach { attr ->
                addCode(
                    "var %N: %T = ",
                    getAttrVarName(attr),
                    attr.types.iosType.copy(nullable = attr.types.isNullable)
                )
                addCode(IOSDefaultAttributeValue.getDefaultValueForAttr(attr))
                addCode("\n")
            }

            beginControlFlow("while (true)")
            addStatement(
                "val tag = %N.stream.readTag()",
                Const.Message.Companion.IOS.WrapperDeserializationFunction.WRAPPER_PARAM
            )
            addStatement("if (tag == 0) break")

            beginControlFlow("when (tag)")

            message.attributes.forEach { attr ->
                when (attr.attributeType) {
                    is Scalar, is Repeated -> {
                        val isPacked = attr.attributeType is Repeated

                        val gpbType = MemberName(
                            COCOAPODS_PROTOBUF_PACKAGE,
                            when (attr.types.protoType) {
                                ProtoType.DOUBLE -> "GPBDataTypeDouble"
                                ProtoType.FLOAT -> "GPBDataTypeFloat"
                                ProtoType.INT_32 -> "GPBDataTypeInt32"
                                ProtoType.INT_64 -> "GPBDataTypeInt64"
                                ProtoType.BOOL -> "GPBDataTypeBool"
                                ProtoType.STRING -> "GPBDataTypeString"
                                ProtoType.MESSAGE -> "GPBDataTypeMessage"
                                ProtoType.ENUM -> "GPBDataTypeEnum"
                                else -> throw IllegalStateException("A")
                            }
                        )

                        addCode(
                            "%M(%L, %M(%M, %L)).toInt()",
                            GPBWireFormatMakeTag,
                            attr.protoId,
                            GPBWireFormatForType,
                            gpbType,
                            isPacked
                        )
                    }

                    is MapType -> TODO()
                }

                addCode(" -> ")

                //The function name of that reads the type from the input stream
                val getScalarReadFunctionName = { ->
                    when (attr.types.protoType) {
                        ProtoType.DOUBLE -> "readDouble"
                        ProtoType.FLOAT -> "readFloat"
                        ProtoType.INT_32 -> "readInt32"
                        ProtoType.INT_64 -> "readInt64"
                        ProtoType.BOOL -> "readBool"
                        ProtoType.STRING -> "readString"
                        ProtoType.ENUM, ProtoType.MAP, ProtoType.MESSAGE -> throw IllegalStateException()
                    }
                }

                when (attr.attributeType) {
                    is Scalar -> {
                        when (attr.types.protoType) {
                            ProtoType.DOUBLE,
                            ProtoType.FLOAT,
                            ProtoType.INT_32,
                            ProtoType.INT_64,
                            ProtoType.BOOL,
                            ProtoType.STRING -> {
                                addCode(
                                    "%N = %N.stream.%N()\n",
                                    getAttrVarName(attr),
                                    Const.Message.Companion.IOS.WrapperDeserializationFunction.WRAPPER_PARAM,
                                    getScalarReadFunctionName()
                                )
                            }

                            ProtoType.MESSAGE -> addCode(
                                "%N = %M(%N, %T.Companion::%N)\n",
                                getAttrVarName(attr),
                                readKMMessage,
                                Const.Message.Companion.IOS.WrapperDeserializationFunction.WRAPPER_PARAM,
                                attr.types.iosType,
                                Const.Message.Companion.IOS.WrapperDeserializationFunction.NAME
                            )

                            ProtoType.ENUM -> addCode(
                                "%N = %T.%N(%N.stream.readEnum())\n",
                                getAttrVarName(attr),
                                attr.types.jsType,
                                Const.Enum.getEnumForNumFunctionName,
                                Const.Message.Companion.IOS.WrapperDeserializationFunction.WRAPPER_PARAM
                            )

                            else -> throw IllegalStateException("C")
                        }

                    }

                    is Repeated -> {
                        beginControlFlow("{")
                        when (attr.types.protoType) {
                            ProtoType.DOUBLE,
                            ProtoType.FLOAT,
                            ProtoType.INT_32,
                            ProtoType.INT_64,
                            ProtoType.BOOL,
                            ProtoType.STRING,
                            ProtoType.ENUM -> {
                                //All packable
                                addStatement(
                                    "val length = %N.stream.readInt32()",
                                    Const.Message.Companion.IOS.WrapperDeserializationFunction.WRAPPER_PARAM
                                )
                                addStatement("val limit = %N.stream.pushLimit(length)")
                                addStatement(
                                    "val _%N = mutableListOf<%T>()",
                                    getAttrVarName(attr),
                                    attr.types.commonType
                                )
                                beginControlFlow("while (!%N.stream.isAtEnd())·{")
                                //Enums need to first get mapped
                                if (attr.types.protoType == ProtoType.ENUM) {
                                    addStatement(
                                        "_%N += %T.%N(%N.stream.readEnum())",
                                        getAttrVarName(attr),
                                        attr.types.commonType,
                                        Const.Message.Companion.IOS.WrapperDeserializationFunction.WRAPPER_PARAM
                                    )
                                } else {
                                    val functionName = getScalarReadFunctionName()

                                    addStatement(
                                        "_%N += %N.stream.%N()",
                                        getAttrVarName(attr),
                                        Const.Message.Companion.IOS.WrapperDeserializationFunction.WRAPPER_PARAM,
                                        functionName
                                    )
                                }

                                endControlFlow()
                            }

                            ProtoType.MESSAGE -> addCode(
                                "%N = %M(%N, %T.Companion::%N)\n",
                                getAttrVarName(attr),
                                readKMMessage,
                                Const.Message.Companion.IOS.WrapperDeserializationFunction.WRAPPER_PARAM,
                                attr.types.iosType,
                                Const.Message.Companion.IOS.WrapperDeserializationFunction.NAME
                            )
                            ProtoType.MAP -> throw IllegalStateException()
                        }
                        endControlFlow()
                    }

                    is MapType -> TODO()
                }
            }

            endControlFlow()

            endControlFlow()

            val paramList = message.attributes.joinToString { it.name }

            addStatement("return %T($paramList)", message.iosType)
        }
    }

    private fun isAttrPackable(attr: ProtoMessageAttribute) = when (attr.types.protoType) {
        ProtoType.DOUBLE,
        ProtoType.FLOAT,
        ProtoType.INT_32,
        ProtoType.INT_64,
        ProtoType.BOOL,
        ProtoType.STRING,
        ProtoType.ENUM -> true

        ProtoType.MESSAGE -> false
        ProtoType.MAP -> throw IllegalStateException()
    }

    override fun applyToEqualsFunction(builder: FunSpec.Builder, message: ProtoMessage, thisClassName: ClassName) {
        builder.addStatement("return false")
    }

    override fun applyToHashCodeFunction(builder: FunSpec.Builder, message: ProtoMessage) {
        builder.addStatement("return 0")
    }

    override fun getChildClassName(parentClass: ClassName?, childName: String): ClassName =
        getChildClassName(parentClass, childName, protoFile.pkg)
}