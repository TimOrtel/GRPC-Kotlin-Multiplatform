package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.proto_file

import com.squareup.kotlinpoet.*
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
                                addParameter(
                                    ParameterSpec
                                        .builder(attr.name, attr.commonType)
                                        .defaultValue(IOSDefaultAttributeValue.getDefaultValueForAttr(attr))
                                        .build()
                                )
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
        return CodeBlock
            .builder()
            .apply {
                message.attributes.forEachIndexed { i, attr ->
                    if (i != 0) add(" + ")
                    when (attr.attributeType) {
                        is Scalar -> {
                            when (attr.types.protoType) {
                                ProtoType.DOUBLE,
                                ProtoType.FLOAT,
                                ProtoType.INT_32,
                                ProtoType.INT_64,
                                ProtoType.BOOL,
                                ProtoType.STRING -> {
                                    val name = when (attr.types.protoType) {
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
                                    val method = MemberName("cocoapods.Protobuf", "GPBCompute${name}Size")
                                    add("%M(%L, %N)", method, attr.protoId, attr.name)
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

                        is MapType -> TODO()
                        is Repeated -> TODO()
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
                                addStatement("stream.%N(%L, %N)", functionName, attr.protoId, attr.name)
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

                            ProtoType.MAP -> TODO()
                            ProtoType.ENUM -> TODO()
                        }
                    }

                    is MapType -> TODO()
                    is Repeated -> TODO()
                }
            }
        }
    }

    private fun buildWrapperDeserializationFunction(builder: FunSpec.Builder, message: ProtoMessage) {
        builder.apply {
            message.attributes.forEach { attr ->
                addCode("var %N: %T = ", attr.name, attr.types.iosType.copy(nullable = attr.types.isNullable))
                addCode(IOSDefaultAttributeValue.getDefaultValueForAttr(attr))
                addCode("\n")
            }

            beginControlFlow("while (true)")
            addStatement("val tag = %N.stream.readTag()", Const.Message.Companion.IOS.WrapperDeserializationFunction.WRAPPER_PARAM)
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
                                    ProtoType.DOUBLE -> "readDouble"
                                    ProtoType.FLOAT -> "readFloat"
                                    ProtoType.INT_32 -> "readInt32"
                                    ProtoType.INT_64 -> "readInt64"
                                    ProtoType.BOOL -> "readBool"
                                    ProtoType.STRING -> "readString"
                                    else -> throw IllegalStateException("B")
                                }

                                addCode(
                                    "%N = %N.stream.%N()\n",
                                    attr.name,
                                    Const.Message.Companion.IOS.WrapperDeserializationFunction.WRAPPER_PARAM,
                                    functionName
                                )
                            }

                            ProtoType.MESSAGE -> addCode(
                                "%N = %M(%N, %T.Companion::%N)\n",
                                attr.name,
                                readKMMessage,
                                Const.Message.Companion.IOS.WrapperDeserializationFunction.WRAPPER_PARAM,
                                attr.types.iosType,
                                Const.Message.Companion.IOS.WrapperDeserializationFunction.NAME
                            )

                            ProtoType.ENUM -> addCode(
                                "%N = %T.%N(%N.stream.readEnum())\n",
                                attr.name,
                                attr.types.jsType,
                                Const.Enum.getEnumForNumFunctionName,
                                Const.Message.Companion.IOS.WrapperDeserializationFunction.WRAPPER_PARAM
                            )
                            else -> throw IllegalStateException("C")
                        }

                    }

                    is MapType -> TODO()
                    is Repeated -> TODO()
                }
            }

            endControlFlow()

            endControlFlow()

            val paramList = message.attributes.joinToString { it.name }

            addStatement("return %T($paramList)", message.iosType)
        }
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