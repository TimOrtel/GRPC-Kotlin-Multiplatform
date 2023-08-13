package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.JSImpl
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.MessageDeserializer
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.ProtoType
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.common.JsCommonFunctionGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.dsl.JsDslBuilder
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.proto_file.JsProtoFileWriter
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.kmMessage
import java.io.File

private val jspb = ClassName("io.github.timortel.kotlin_multiplatform_grpc_lib", "JSPB")
private val jspbMessage = jspb.nestedClass("Message")
private val jspbWriter = jspb.nestedClass("BinaryWriter")
private val jspbReader = jspb.nestedClass("BinaryReader")
private val jspbMap = jspb.nestedClass("Map")

val functionWrapper = MemberName("io.github.timortel.kotlin_multiplatform_grpc_lib.util", "wrap")

const val objPropertyName = "obj"

fun generateBridgeClass(parentClass: ClassName?, message: ProtoMessage): TypeSpec {
    val prototypePropertyName = "prototype"
    val newFunctionName = "new"

    val jsImplClassName = parentClass?.nestedClass("JS_" + message.name) ?: ClassName(
        message.pkg,
        "JS_" + message.name
    )

    return TypeSpec
        .classBuilder(jsImplClassName)
        .addSuperinterface(JSImpl)
        .primaryConstructor(
            FunSpec
                .constructorBuilder()
                .addParameter(
                    ParameterSpec
                        .builder(objPropertyName, Dynamic)
                        .defaultValue("new()")
                        .build()
                )
                .build()
        )
        .addProperty(
            PropertySpec
                .builder(objPropertyName, Dynamic)
                .initializer(objPropertyName)
                .build()
        )
        .addType(
            TypeSpec
                .companionObjectBuilder()
                .addSuperinterface(MessageDeserializer.parameterizedBy(jsImplClassName))
                .addProperty(prototypePropertyName, Dynamic)
                .addInitializerBlock(
                    CodeBlock
                        .builder()
                        .addStatement("val jspb = %T", jspb)
                        .addStatement("val messageClass = js(\"function()·{·jspb.Message.initialize(this,·[],·0,·-1,·null,·null);·}\")")
                        .addStatement("%T.inherits(messageClass, %T)", jspb, jspbMessage)
                        .addStatement("%N = messageClass", prototypePropertyName)
                        .build()
                )
                .addFunction(
                    FunSpec
                        .builder(newFunctionName)
                        .returns(Dynamic)
                        .addStatement("val prototype = %N", prototypePropertyName)
                        .addStatement("return js(\"new prototype\")")
                        .build()
                )
                .addFunction(
                    FunSpec
                        .builder("serializeBinaryToWriter")
                        .addParameter("msg", Dynamic)
                        .addParameter("writer", jspbWriter)
                        .addCode(writeSerializeBinaryToWriter(message))
                        .build()
                )
                .addFunction(
                    FunSpec
                        .builder("deserializeBinaryFromReader")
                        .addParameter("msg", Dynamic)
                        .addParameter("reader", jspbReader)
                        .returns(Dynamic)
                        .addCode(writeDeserializeBinaryFromReader(message))
                        .build()
                )
                .addFunction(
                    FunSpec
                        .builder("deserializeBinary")
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter("bytes", Dynamic)
                        .returns(message.jsType)
                        .addStatement("val reader = %T(bytes)", jspbReader)
                        .addStatement("val msg = %T()", message.jsType)
                        .addStatement(
                            "deserializeBinaryFromReader(msg.%N, reader)",
                            objPropertyName
                        )
                        .addStatement("return msg")
                        .build()
                )

                .build()
        )
        .addFunction(
            FunSpec
                .builder("serializeBinary")
                .addModifiers(KModifier.OVERRIDE)
                .returns(Dynamic)
                .addStatement("val writer = %T()", jspbWriter)
                .addStatement("serializeBinaryToWriter(this.%N, writer)", objPropertyName)
                .addStatement("return writer.getResultBuffer()")
                .build()
        )
        .apply {
            // Add getters and setters

            message.attributes.forEach { attr ->
                when (attr.attributeType) {
                    is Scalar -> {
                        val getterCodeBlock = when (attr.types.protoType) {
                            ProtoType.DOUBLE, ProtoType.FLOAT -> {
                                val isDouble = attr.types.protoType == ProtoType.DOUBLE

                                CodeBlock.of(
                                    "return %T.%N(%N, %L, %L) as %T",
                                    jspbMessage,
                                    "getFloatingPointFieldWithDefault",
                                    objPropertyName,
                                    attr.protoId,
                                    if (isDouble) 0.0 else 0f,
                                    if (isDouble) Double::class else Float::class
                                )
                            }

                            ProtoType.INT_32, ProtoType.INT_64 -> {
                                val isLong = attr.types.protoType == ProtoType.INT_64

                                val code = if (isLong) {
                                    "return %T.%N(%N, %L, 0L)"
                                } else "return %T.%N(%N, %L, 0) as Int"

                                CodeBlock.of(
                                    code,
                                    jspbMessage,
                                    "getFieldWithDefault",
                                    objPropertyName,
                                    attr.protoId
                                )
                            }

                            ProtoType.BOOL ->
                                CodeBlock.of(
                                    "return %T.%N(%N, %L, %L)",
                                    jspbMessage,
                                    "getBooleanFieldWithDefault",
                                    objPropertyName,
                                    attr.protoId,
                                    false
                                )

                            ProtoType.STRING ->
                                CodeBlock.of(
                                    "return %T.%N(%N, %L, \"\") as String",
                                    jspbMessage,
                                    "getFieldWithDefault",
                                    objPropertyName,
                                    attr.protoId
                                )

                            ProtoType.MESSAGE ->
                                CodeBlock.of(
                                    "return %T(%T.%N(%N, %T.%N, %L))",
                                    attr.types.jsType,
                                    jspbMessage,
                                    "getWrapperField",
                                    objPropertyName,
                                    attr.types.jsType,
                                    prototypePropertyName,
                                    attr.protoId
                                )

                            ProtoType.ENUM ->
                                CodeBlock.of(
                                    "return %T.%N(%N, %L, %L)",
                                    jspbMessage,
                                    "getFieldWithDefault",
                                    objPropertyName,
                                    attr.protoId,
                                    0
                                )

                            ProtoType.MAP -> throw IllegalStateException()
                        }

                        val setterCodeBlock = when (attr.types.protoType) {
                            ProtoType.DOUBLE, ProtoType.FLOAT, ProtoType.INT_32, ProtoType.INT_64, ProtoType.BOOL, ProtoType.STRING, ProtoType.ENUM -> {
                                val setterName = when (attr.types.protoType) {
                                    ProtoType.DOUBLE, ProtoType.FLOAT -> "setProto3FloatField"
                                    ProtoType.INT_32, ProtoType.INT_64 -> "setProto3IntField"
                                    ProtoType.BOOL -> "setProto3BooleanField"
                                    ProtoType.STRING -> "setProto3StringField"
                                    ProtoType.ENUM -> "setProto3EnumField"
                                    else -> throw IllegalStateException()
                                }

                                CodeBlock.of(
                                    "%T.%N(%N, %L, %N)",
                                    jspbMessage,
                                    setterName,
                                    objPropertyName,
                                    attr.protoId,
                                    "value"
                                )
                            }

                            ProtoType.MAP -> throw IllegalStateException()
                            ProtoType.MESSAGE ->
                                CodeBlock.of(
                                    "%T.%N(%N, %L, %N.%N)",
                                    jspbMessage,
                                    "setWrapperField",
                                    objPropertyName,
                                    attr.protoId,
                                    "value",
                                    objPropertyName
                                )
                        }

                        // getter
                        addFunction(
                            FunSpec
                                .builder(
                                    Const.Message.Attribute.Scalar.JS.getFunction(
                                        message,
                                        attr
                                    ).simpleName
                                )
                                .returns(attr.types.jsType)
                                .addCode(getterCodeBlock)
                                .build()
                        )

                        // setter
                        addFunction(
                            FunSpec
                                .builder(
                                    Const.Message.Attribute.Scalar.JS.setFunction(
                                        message,
                                        attr
                                    ).simpleName
                                )
                                .addParameter("value", attr.types.jsType.copy(nullable = false))
                                .addCode(setterCodeBlock)
                                .build()
                        )

                        // hasField
                        if (attr.types.isNullable) {
                            addFunction(
                                FunSpec
                                    .builder(
                                        Const.Message.Attribute.Scalar.JS.getHasFunction(
                                            message,
                                            attr
                                        ).simpleName
                                    )
                                    .returns(Boolean::class)
                                    .addCode(
                                        "return %T.getField(%N, %L) != null",
                                        jspbMessage,
                                        objPropertyName,
                                        attr.protoId
                                    )
                                    .build()
                            )
                        }
                    }

                    is Repeated -> {
                        val setterCode = when (attr.types.protoType) {
                            ProtoType.DOUBLE, ProtoType.FLOAT, ProtoType.INT_32, ProtoType.INT_64, ProtoType.BOOL, ProtoType.STRING, ProtoType.ENUM ->
                                CodeBlock.of(
                                    "%T.setField(%N, %L, %N)",
                                    jspbMessage,
                                    objPropertyName,
                                    attr.protoId,
                                    "values"
                                )

                            ProtoType.MESSAGE -> CodeBlock.of(
                                "%T.setRepeatedWrapperField(%N, %L, %N.map·{ it.%N }.toTypedArray())",
                                jspbMessage,
                                objPropertyName,
                                attr.protoId,
                                "values",
                                objPropertyName
                            )

                            ProtoType.MAP -> throw IllegalStateException()
                        }

                        val getterCode = when (attr.types.protoType) {
                            ProtoType.DOUBLE, ProtoType.FLOAT -> {
                                val isDouble = attr.types.protoType == ProtoType.DOUBLE

                                CodeBlock.of(
                                    "return %T.%N(%N, %L) as Array<%T>",
                                    jspbMessage,
                                    "getRepeatedFloatingPointField",
                                    objPropertyName,
                                    attr.protoId,
                                    if (isDouble) DOUBLE else FLOAT
                                )
                            }

                            ProtoType.INT_32, ProtoType.INT_64 -> {
                                val isLong = attr.types.protoType == ProtoType.INT_64

                                CodeBlock.of(
                                    "return %T.%N(%N, %L) as Array<%T>",
                                    jspbMessage,
                                    "getRepeatedField",
                                    objPropertyName,
                                    attr.protoId,
                                    if (isLong) NUMBER else INT
                                )
                            }

                            ProtoType.BOOL ->
                                CodeBlock.of(
                                    "return %T.%N(%N, %L) as Array<Boolean>",
                                    jspbMessage,
                                    "getRepeatedBooleanField",
                                    objPropertyName,
                                    attr.protoId
                                )

                            ProtoType.STRING ->
                                CodeBlock.of(
                                    "return %T.%N(%N, %L) as Array<String>",
                                    jspbMessage,
                                    "getRepeatedField",
                                    objPropertyName,
                                    attr.protoId
                                )

                            ProtoType.MESSAGE ->
                                CodeBlock.of(
                                    "return %T.%N(%N, %T.%N, %L).map·{ %T(it) }.toTypedArray() as Array<%T>",
                                    jspbMessage,
                                    "getRepeatedWrapperField",
                                    objPropertyName,
                                    attr.types.jsType,
                                    prototypePropertyName,
                                    attr.protoId,
                                    attr.types.jsType,
                                    attr.types.jsType
                                )

                            ProtoType.ENUM ->
                                CodeBlock.of(
                                    "return %T.%N(%N, %L) as Array<Int>",
                                    jspbMessage,
                                    "getRepeatedField",
                                    objPropertyName,
                                    attr.protoId
                                )

                            ProtoType.MAP -> throw IllegalStateException()
                        }

                        val arrayType = ClassName("kotlin", "Array")
                            .parameterizedBy(
                                //INT64 is broken, therefore we cast it to number instead.
                                if (attr.types.protoType == ProtoType.INT_64) NUMBER else attr.types.jsType
                            )
                        // List getter
                        addFunction(
                            FunSpec
                                .builder(
                                    Const.Message.Attribute.Repeated.JS.getListFunctionName(
                                        attr
                                    )
                                )
                                .returns(arrayType)
                                .addCode(getterCode)
                                .build()
                        )
                        // List setter
                        addFunction(
                            FunSpec
                                .builder(
                                    Const.Message.Attribute.Repeated.JS.setListFunctionName(
                                        attr
                                    )
                                )
                                .addParameter("values", arrayType)
                                .addCode(setterCode)
                                .build()
                        )
                    }

                    is MapType -> {
                        //Get map
                        addFunction(
                            FunSpec
                                .builder(Const.Message.Attribute.Map.JS.getMapFunctionName(attr))
                                .addParameter("noLazyCreate", Boolean::class)
                                .returns(Dynamic)
                                .addCode(
                                    CodeBlock.builder().apply {
                                        add(
                                            "return %T.getMapField(%N, %L, noLazyCreate, ",
                                            jspbMessage,
                                            objPropertyName,
                                            attr.protoId
                                        )

                                        if (attr.attributeType.valueTypes.protoType == ProtoType.MESSAGE) {
                                            add(
                                                "%T.%N", attr.attributeType.valueTypes.jsType,
                                                prototypePropertyName
                                            )
                                        } else {
                                            add("null")
                                        }

                                        add(")")
                                    }.build()
                                )
                                .build()
                        )
                    }
                }
            }

            //Add get case function for each oneof
            message.oneOfs.forEach { oneOf ->
                addFunction(
                    FunSpec
                        .builder(Const.Message.OneOf.JS.getCaseFunctionName(oneOf))
                        .returns(Int::class)
                        .apply {
                            addCode(
                                "return %T.computeOneofCase(%N, arrayOf(",
                                jspbMessage,
                                objPropertyName,
                            )

                            addCode(
                                oneOf.attributes.joinToString { it.protoId.toString() },
                            )

                            addCode("))")
                        }
                        .build()
                )
            }

            message.children.forEach { childMessage ->
                addType(generateBridgeClass(jsImplClassName, childMessage))
            }
        }
        .build()
}

private fun writeSerializeBinaryToWriter(message: ProtoMessage): CodeBlock {
    return CodeBlock.builder().apply {
        addStatement("val message = %T(msg)", message.jsType)
        addStatement("var temp: dynamic = undefined")
        message.attributes.forEach { attr ->
            when (attr.attributeType) {
                is Scalar -> {
                    val (writerFunction, alreadyHandled) = if (attr.types.protoType != ProtoType.MESSAGE) {
                        val getter = Const.Message.Attribute.Scalar.JS.getFunction(message, attr)
                        addStatement("temp = message.%N()", getter)
                        add("if (message.%N()", getter)

                        when (attr.types.protoType) {
                            ProtoType.DOUBLE -> {
                                beginControlFlow(" != 0.0)")
                                "writeDouble" to false
                            }

                            ProtoType.FLOAT -> {
                                beginControlFlow(" != 0f)")
                                "writeFloat" to false
                            }

                            ProtoType.INT_32 -> {
                                beginControlFlow(" != 0)")
                                "writeInt32" to false
                            }

                            ProtoType.INT_64 -> {
                                beginControlFlow(" != 0L)")
                                "writeInt64" to false
                            }

                            ProtoType.BOOL -> {
                                beginControlFlow(")")
                                "writeBool" to false
                            }

                            ProtoType.STRING -> {
                                beginControlFlow(".length > 0)")
                                "writeString" to false
                            }

                            ProtoType.MAP, ProtoType.MESSAGE -> throw IllegalStateException()

                            ProtoType.ENUM -> {
                                beginControlFlow(" != 0)")
                                "writeEnum" to false
                            }
                        }
                    } else {
                        beginControlFlow(
                            "if (message.%N())",
                            Const.Message.Attribute.Scalar.JS.getHasFunction(message, attr).simpleName
                        )
                        addStatement(
                            "writer.writeMessage(%L, temp.%N, %T.Companion::serializeBinaryToWriter)",
                            attr.protoId,
                            objPropertyName,
                            attr.types.jsType,
                        )
                        endControlFlow()
                        "writeMessage" to true
                    }

                    if (!alreadyHandled) {
                        addStatement("writer.%N(%L, temp)", writerFunction, attr.protoId)
                        endControlFlow()
                    }
                }

                is Repeated -> {
                    addStatement(
                        "temp = message.%N()",
                        Const.Message.Attribute.Repeated.JS.getListFunctionName(attr)
                    )
                    beginControlFlow("if (temp.length > 0)")
                    addStatement(
                        "writer.%N(%L, temp",
                        getRepeatedWriterFunction(attr.types.protoType),
                        attr.protoId
                    )

                    if (attr.types.protoType == ProtoType.MESSAGE) {
                        add(", %T.Companion::serializeBinaryToWriter", attr.types.jsType)
                    }

                    add(")")

                    endControlFlow()
                }

                is MapType -> {
                    addStatement(
                        "temp = message.%N(false)",
                        Const.Message.Attribute.Map.JS.getMapFunctionName(attr)
                    )
                    beginControlFlow("if (temp != null && temp.getLength() > 0)")
                    add(
                        "temp.serializeBinary(%L, writer, %T::%N.%M(writer), %T::%N.%M(writer), ",
                        attr.protoId,
                        jspbWriter,
                        getWriterFunction(attr.attributeType.keyTypes.protoType),
                        functionWrapper,
                        jspbWriter,
                        getWriterFunction(attr.attributeType.valueTypes.protoType),
                        functionWrapper
                    )
                    if (attr.attributeType.valueTypes.protoType == ProtoType.MESSAGE) {
                        add(
                            "%T.Companion::serializeBinaryToWriter",
                            attr.attributeType.valueTypes.jsType
                        )
                    } else {
                        add("null")
                    }
                    add(")")
                    endControlFlow()
                }
            }
        }
    }.build()
}

private fun getWriterFunction(protoType: ProtoType) = when (protoType) {
    ProtoType.DOUBLE -> "writeDouble"
    ProtoType.FLOAT -> "writeFloat"
    ProtoType.INT_32 -> "writeInt32"
    ProtoType.INT_64 -> "writeInt64"
    ProtoType.BOOL -> "writeBool"
    ProtoType.STRING -> "writeString"
    ProtoType.MAP -> throw IllegalArgumentException()
    ProtoType.MESSAGE -> "writeMessage"
    ProtoType.ENUM -> "writeEnum"
}

private fun getRepeatedWriterFunction(protoType: ProtoType) = when (protoType) {
    ProtoType.DOUBLE -> "writePackedDouble"
    ProtoType.FLOAT -> "writePackedFloat"
    ProtoType.INT_32 -> "writePackedInt32"
    ProtoType.INT_64 -> "writePackedInt64"
    ProtoType.BOOL -> "writePackedBool"
    ProtoType.STRING -> "writeRepeatedString"
    ProtoType.MAP -> throw IllegalArgumentException()
    ProtoType.MESSAGE -> "writeRepeatedMessage"
    ProtoType.ENUM -> "writePackedEnum"
}

/**
 * Generates the code for deserializeBinaryFromReader. Mimics the code generation of protoc for js
 */
private fun writeDeserializeBinaryFromReader(message: ProtoMessage): CodeBlock {
    val getRepeatedListVarName =
        { repeatedAttr: ProtoMessageAttribute -> "${repeatedAttr.name.decapitalize()}List" }

    return CodeBlock.builder().apply {
        addStatement("val message = %T(msg)", message.jsType)

        message.attributes.filter { it.attributeType is Repeated }.forEach { repeatedAttr ->
            addStatement(
                "val %N = mutableListOf<%T>()",
                getRepeatedListVarName(repeatedAttr),
                repeatedAttr.types.jsType
            )
        }

        beginControlFlow("while (reader.nextField())")

        beginControlFlow("if (reader.isEndGroup())")
        addStatement("break;")
        endControlFlow()

        addStatement("val field = reader.getFieldNumber()")
        beginControlFlow("when (field)")

        message.attributes.forEach { attr ->
            beginControlFlow("%L ->", attr.protoId)
            when (attr.attributeType) {
                is Scalar -> {
                    when (attr.types.protoType) {
                        ProtoType.DOUBLE, ProtoType.FLOAT, ProtoType.INT_32, ProtoType.BOOL, ProtoType.ENUM, ProtoType.STRING -> {
                            addStatement(
                                "val value = reader.%N()",
                                getScalarReadFunctionName(attr.types.protoType)
                            )
                            addStatement(
                                "message.%N(value)",
                                Const.Message.Attribute.Scalar.JS.setFunction(message, attr)
                            )
                        }

                        ProtoType.INT_64 -> {
                            addStatement("//Handle weird behaviour of Long")
                            addStatement("val value = (reader.readInt64() as %T).toLong()", NUMBER)
                            addStatement(
                                "message.%N(value)",
                                Const.Message.Attribute.Scalar.JS.setFunction(message, attr)
                            )
                        }

                        ProtoType.MESSAGE -> {
                            addStatement("val value = %T()\n", attr.types.jsType)
                            addStatement(
                                "reader.readMessage(value.%N, %T.Companion::deserializeBinaryFromReader)",
                                objPropertyName,
                                attr.types.jsType
                            )
                            addStatement(
                                "message.%N(value)",
                                Const.Message.Attribute.Scalar.JS.setFunction(message, attr)
                            )
                        }

                        ProtoType.MAP -> throw IllegalStateException()
                    }
                }

                is Repeated -> {
                    when (attr.types.protoType) {
                        ProtoType.DOUBLE, ProtoType.FLOAT, ProtoType.INT_32, ProtoType.INT_64, ProtoType.BOOL, ProtoType.ENUM -> {
                            addStatement(
                                "val value = if (reader.isDelimited()) (reader.%N() as Array<%T>).toList() else listOf(reader.%N())\n",
                                getRepeatedReadFunctionName(attr.types.protoType),
                                attr.types.jsType,
                                getScalarReadFunctionName(attr.types.protoType)
                            )

                            addStatement("%N += value", getRepeatedListVarName(attr))
                        }

                        ProtoType.STRING -> {
                            addStatement("%N += reader.readString()", getRepeatedListVarName(attr))
                        }

                        ProtoType.MESSAGE -> {
                            addStatement("val value = %T()", attr.types.jsType)
                            addStatement(
                                "reader.readMessage(value.%N, %T.Companion::deserializeBinaryFromReader)",
                                objPropertyName,
                                attr.types.jsType
                            )
                            addStatement("%N += value", getRepeatedListVarName(attr))
                        }

                        ProtoType.MAP -> throw IllegalStateException()
                    }
                }

                is MapType -> {
                    addStatement(
                        "val value = message.%N(false)",
                        Const.Message.Attribute.Map.JS.getMapFunctionName(attr)
                    )
                    addStatement("reader.readMessage(value,·{·message:·dynamic,·reader:·dynamic ->")
                    addStatement(
                        "%T.deserializeBinary(message, reader, %T::%N.%M(reader), %T::%N.%M(reader), ",
                        jspbMap,
                        jspbReader,
                        getScalarReadFunctionName(attr.attributeType.keyTypes.protoType),
                        functionWrapper,
                        jspbReader,
                        getScalarReadFunctionName(attr.attributeType.valueTypes.protoType),
                        functionWrapper
                    )

                    //Add the reader callback, for non message types = null
                    if (attr.attributeType.valueTypes.protoType == ProtoType.MESSAGE) {
                        add(
                            "%T::deserializeBinaryFromReader, ",
                            attr.attributeType.valueTypes.jsType
                        )
                    } else {
                        add("null, ")
                    }

                    //The key type cannot be another map or message.
                    if (attr.attributeType.keyTypes.protoType == ProtoType.STRING) {
                        add("\"\", ")
                    } else {
                        add("%L, ", getDefaultValueLiteral(attr.attributeType.keyTypes.protoType))
                    }

                    if (attr.attributeType.valueTypes.protoType == ProtoType.MESSAGE) {
                        add("%T().%N", attr.attributeType.valueTypes.jsType, objPropertyName)
                    } else if (attr.attributeType.valueTypes.protoType == ProtoType.STRING) {
                        add("\"\"")
                    } else {
                        add("%L", getDefaultValueLiteral(attr.attributeType.valueTypes.protoType))
                    }

                    add(")\n")
                    add("})\n")
                }
            }
            endControlFlow()
        }

        addStatement("else -> reader.skipField()")

        endControlFlow()

        endControlFlow()

        //In the end set the repeated fields
        message.attributes.filter { it.attributeType is Repeated }.forEach { repeatedAttr ->
            addStatement(
                "message.%N(%N.toTypedArray())",
                Const.Message.Attribute.Repeated.JS.setListFunctionName(repeatedAttr),
                getRepeatedListVarName(repeatedAttr)
            )
        }

        addStatement("return msg")
    }.build()
}

/**
 * @return the default value for unset values of the given prototype. E.g. for string the empty string, for integers 0 etc.
 */
private fun getDefaultValueLiteral(protoType: ProtoType) = when (protoType) {
    ProtoType.DOUBLE -> 0.0
    ProtoType.FLOAT -> 0f
    ProtoType.INT_32 -> 0
    ProtoType.INT_64 -> 0L
    ProtoType.BOOL -> false
    ProtoType.STRING -> ""
    ProtoType.MAP -> throw IllegalArgumentException("Maps never need a default type")
    ProtoType.MESSAGE -> throw IllegalArgumentException("Message default cannot be represented as a literal")
    ProtoType.ENUM -> 0
}

private fun getScalarReadFunctionName(protoType: ProtoType) = when (protoType) {
    ProtoType.DOUBLE -> "readDouble"
    ProtoType.FLOAT -> "readFloat"
    ProtoType.INT_32 -> "readInt32"
    ProtoType.INT_64 -> "readInt64"
    ProtoType.BOOL -> "readBool"
    ProtoType.ENUM -> "readEnum"
    ProtoType.STRING -> "readString"
    ProtoType.MAP -> throw IllegalArgumentException("Map does not have a read function.")
    ProtoType.MESSAGE -> "readMessage"
}

private fun getRepeatedReadFunctionName(protoType: ProtoType) = when (protoType) {
    ProtoType.DOUBLE -> "readPackedDouble"
    ProtoType.FLOAT -> "readPackedFloat"
    ProtoType.INT_32 -> "readPackedInt32"
    ProtoType.INT_64 -> "readPackedInt64"
    ProtoType.BOOL -> "readPackedBool"
    ProtoType.ENUM -> "readPackedEnum"
    ProtoType.MAP, ProtoType.MESSAGE, ProtoType.STRING -> throw IllegalArgumentException("Map, string and message do not have repeated read functions.")
}

fun writeJsFiles(protoFile: ProtoFile, jsOutputDir: File) {
    JsProtoFileWriter(protoFile).writeFile(jsOutputDir)

    //JS Bridge
    FileSpec
        .builder(protoFile.pkg, protoFile.fileNameWithoutExtension + "_js_bridge")
        .apply {
            //Add an open class for each message

            protoFile.messages.forEach { message ->
                addType(
                    generateBridgeClass(null, message)
                )
            }
        }
        .build()
        .writeTo(jsOutputDir)

    //Js common helper
    FileSpec
        .builder(protoFile.pkg, protoFile.fileNameWithoutExtension + "_js_helper")
        .apply {
            JsCommonFunctionGenerator(this).generateCommonGetter(protoFile.messages)
        }
        .build()
        .writeTo(jsOutputDir)

    writeDSLBuilder(protoFile, JsDslBuilder, jsOutputDir)
}