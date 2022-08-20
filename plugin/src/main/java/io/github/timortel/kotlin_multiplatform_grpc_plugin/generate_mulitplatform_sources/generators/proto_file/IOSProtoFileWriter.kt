package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.proto_file

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
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

    override fun applyToClass(builder: TypeSpec.Builder, message: ProtoMessage, messageClassName: ClassName) {
        builder.apply {
            primaryConstructor(
                FunSpec
                    .constructorBuilder()
                    .apply {
                        //one of attributes do not get a parameter, as they get the one of parameter
                        message.attributes.filter { !it.isOneOfAttribute }.forEach { attr ->
                            when (attr.attributeType) {
                                is Scalar -> {
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
                                                .defaultValue(attr.commonDefaultValue(false))
                                                .build()
                                        )
                                    }
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

                                is MapType -> {
                                    addParameter(
                                        ParameterSpec
                                            .builder(
                                                Const.Message.Attribute.propertyName(message, attr),
                                                MAP.parameterizedBy(
                                                    attr.attributeType.keyTypes.iosType,
                                                    attr.attributeType.valueTypes.iosType
                                                )
                                            )
                                            .defaultValue("emptyMap()")
                                            .build()
                                    )
                                }
                            }
                        }

                        message.oneOfs.forEach { oneOf ->
                            addParameter(
                                ParameterSpec
                                    .builder(
                                        Const.Message.OneOf.propertyName(message, oneOf),
                                        Const.Message.OneOf.parentSealedClassName(message, oneOf)
                                    )
                                    .defaultValue(oneOf.defaultValue(message))
                                    .build()
                            )
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
                    .addSuperinterface(MessageDeserializer.parameterizedBy(messageClassName))
                    .addFunction(
                        //The function that builds the message from NSData
                        FunSpec
                            .builder(Const.Message.Companion.IOS.DataDeserializationFunction.NAME)
                            .addModifiers(KModifier.OVERRIDE)
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
                val onlyNonOneOfAttributes = message.attributes.filter { !it.isOneOfAttribute }

                onlyNonOneOfAttributes.forEachIndexed { i, attr ->
                    if (i != 0) add("·+\n")
                    when (attr.attributeType) {
                        is Scalar -> {
                            add("if·(%N·==·", Const.Message.Attribute.propertyName(message, attr))
                            add(attr.commonDefaultValue(false))
                            add(")·0u·else·{ ")
                            add(getCodeForRequiredSizeForScalarAttributeC(attr))
                            add(" }")
                        }

                        is Repeated -> {
                            val attrFieldName = Const.Message.Attribute.Repeated.listPropertyName(attr)

                            add("if (%N.isEmpty()) 0u else %N.let·{·e ->\n", attrFieldName, attrFieldName)
                            beginControlFlow("val dataSize = e.sumOf·{")
                            when (attr.types.protoType) {
                                ProtoType.DOUBLE,
                                ProtoType.FLOAT,
                                ProtoType.INT_32,
                                ProtoType.INT_64,
                                ProtoType.BOOL,
                                ProtoType.STRING -> add(
                                    "%M(it)",
                                    getComputeDataTypeSizeMember(attr.types.protoType, false)
                                )

                                ProtoType.MESSAGE -> add(
                                    "%M(it)",
                                    MemberName(
                                        "io.github.timortel.kotlin_multiplatform_grpc_lib.message",
                                        "computeMessageSizeNoTag"
                                    )
                                )

                                ProtoType.ENUM -> add(
                                    "%M(it.%N)",
                                    MemberName("cocoapods.Protobuf", "GPBComputeEnumSizeNoTag"),
                                    Const.Enum.VALUE_PROPERTY_NAME
                                )

                                ProtoType.MAP -> throw IllegalStateException()
                            }
                            endControlFlow()
                            addStatement("val tagSize = %M(%L)", GPBComputeTagSize, attr.protoId)

                            val isPackable = isAttrPackable(attr)

                            if (isPackable) {
                                addStatement("dataSize + tagSize + %M(tagSize)", GPBComputeSizeTSizeAsInt32NoTag)
                            } else {
                                addStatement("dataSize + %N.size.toULong() * tagSize", attrFieldName)
                            }

                            add("\n}")
                        }

                        is MapType -> {
                            add(
                                "%M(%L, %N, ::%M, ",
                                computeMapSize,
                                attr.protoId,
                                Const.Message.Attribute.propertyName(message, attr),
                                getComputeDataTypeSizeMember(attr.attributeType.keyTypes.protoType, true)
                            )

                            add(getComputeMapValueRequiredSizeCode(attr.attributeType.valueTypes))

                            add(")")
                        }
                    }
                }

                message.oneOfs.forEachIndexed { index, oneOf ->
                    if (index != 0 || onlyNonOneOfAttributes.isNotEmpty()) add("·+\n")
                    add(
                        "%N.%N",
                        Const.Message.OneOf.propertyName(message, oneOf),
                        Const.Message.OneOf.IOS.REQUIRED_SIZE_PROPERTY_NAME
                    )
                }
            }
            .build()
    }

    /**
     * Generate the serialize function, adds a write call for each attribute
     */
    private fun buildSerializeFunction(builder: FunSpec.Builder, message: ProtoMessage) {
        builder.apply {
            message.attributes.filter { !it.isOneOfAttribute }.forEach { attr ->
                when (attr.attributeType) {
                    is Scalar -> {
                        addCode("if·(%N·!=·", Const.Message.Attribute.propertyName(message, attr))
                        addCode(attr.commonDefaultValue(false))
                        beginControlFlow(")·{ ")
                        addCode(
                            getWriteScalarFieldCode(
                                message,
                                attr,
                                Const.Message.IOS.SerializeFunction.STREAM_PARAM
                            )
                        )
                        endControlFlow()
                    }

                    is Repeated -> {
                        val writeListPartialFunctionName = when (attr.types.protoType) {
                            ProtoType.DOUBLE -> "Double"
                            ProtoType.FLOAT -> "Float"
                            ProtoType.INT_32 -> "Int32"
                            ProtoType.INT_64 -> "Int64"
                            ProtoType.BOOL -> "Bool"
                            ProtoType.STRING -> "String"
                            ProtoType.MESSAGE -> "Message"
                            ProtoType.ENUM -> "Enum"
                            ProtoType.MAP -> throw IllegalStateException()
                        }

                        val writeListFunction = MemberName(
                            "io.github.timortel.kotlin_multiplatform_grpc_lib.io",
                            "write${writeListPartialFunctionName}List"
                        )

                        when (attr.types.protoType) {
                            ProtoType.DOUBLE,
                            ProtoType.FLOAT,
                            ProtoType.INT_32,
                            ProtoType.INT_64,
                            ProtoType.BOOL -> {
                                //Write packed.
                                // From GPBDescriptor.m: GPBWireFormatForType(description->dataType,
                                //                                  ((description->flags & GPBFieldPacked) != 0))
                                addStatement(
                                    "%M(%N, %L, %N, %M(%L, %M(%M, true)))",
                                    writeListFunction,
                                    Const.Message.IOS.SerializeFunction.STREAM_PARAM,
                                    attr.protoId,
                                    Const.Message.Attribute.Repeated.listPropertyName(attr),
                                    GPBWireFormatMakeTag,
                                    attr.protoId,
                                    GPBWireFormatForType,
                                    getGPBDataTypeForProtoType(attr.types.protoType)
                                )
                            }

                            ProtoType.ENUM -> {
                                addStatement(
                                    "%M(%N, %L, %N.map·{ it.%N }, %M(%L, %M(%M, true)))",
                                    writeListFunction,
                                    Const.Message.IOS.SerializeFunction.STREAM_PARAM,
                                    attr.protoId,
                                    Const.Message.Attribute.Repeated.listPropertyName(attr),
                                    Const.Enum.VALUE_PROPERTY_NAME,
                                    GPBWireFormatMakeTag,
                                    attr.protoId,
                                    GPBWireFormatForType,
                                    getGPBDataTypeForProtoType(attr.types.protoType)
                                )
                            }

                            ProtoType.STRING -> {
                                //Write unpacked. 0 means unpacked
                                addStatement(
                                    "%M(%N, %L, %N, 0u)",
                                    writeListFunction,
                                    Const.Message.IOS.SerializeFunction.STREAM_PARAM,
                                    attr.protoId,
                                    Const.Message.Attribute.propertyName(message, attr)
                                )
                            }

                            ProtoType.MESSAGE -> {
                                addStatement(
                                    "%M(%N, %L, %N)",
                                    writeListFunction,
                                    Const.Message.IOS.SerializeFunction.STREAM_PARAM,
                                    attr.protoId,
                                    Const.Message.Attribute.propertyName(message, attr)
                                )
                            }

                            ProtoType.MAP -> throw IllegalStateException()
                        }
                    }

                    is MapType -> {
                        addCode(
                            "%M(%N, %L, %N, ::%M, ",
                            writeMap,
                            Const.Message.IOS.SerializeFunction.STREAM_PARAM,
                            attr.protoId,
                            Const.Message.Attribute.propertyName(message, attr),
                            getComputeDataTypeSizeMember(attr.attributeType.keyTypes.protoType, true),
                        )

                        addCode(getComputeMapValueRequiredSizeCode(attr.attributeType.valueTypes))

                        addCode(", ")

                        val addWriteScalarCode = { types: Types ->
                            addCode(
                                "%T::%N",
                                GPBCodedOutputStream,
                                getWriteScalarFunctionName(types.protoType)
                            )
                        }

                        val addWriteEnumCode = {
                            addCode(
                                "{·fieldNumber,·it·-> writeEnum(fieldNumber, it.%N)·}",
                                Const.Enum.VALUE_PROPERTY_NAME
                            )
                        }

                        when (attr.attributeType.keyTypes.protoType) {
                            ProtoType.INT_32,
                            ProtoType.INT_64,
                            ProtoType.BOOL,
                            ProtoType.STRING -> addWriteScalarCode(attr.attributeType.keyTypes)

                            ProtoType.DOUBLE, ProtoType.FLOAT, ProtoType.MESSAGE, ProtoType.MAP, ProtoType.ENUM -> throw IllegalStateException(
                                "Illegal Map Key Type: ${attr.attributeType.keyTypes.protoType}"
                            )
                        }

                        addCode(", ")

                        when (attr.attributeType.valueTypes.protoType) {
                            ProtoType.DOUBLE,
                            ProtoType.FLOAT,
                            ProtoType.INT_32,
                            ProtoType.INT_64,
                            ProtoType.BOOL,
                            ProtoType.STRING -> addWriteScalarCode(attr.attributeType.valueTypes)

                            ProtoType.ENUM -> addWriteEnumCode()
                            ProtoType.MESSAGE -> addCode(
                                "{·fieldNumber,·msg·-> %M(%N, fieldNumber, msg)·}",
                                writeKMMessage,
                                Const.Message.IOS.SerializeFunction.STREAM_PARAM
                            )

                            ProtoType.MAP -> throw IllegalStateException()
                        }

                        addCode(")\n")
                    }
                }
            }

            message.oneOfs.forEach { oneOf ->
                addStatement(
                    "%N.%N(%N)",
                    Const.Message.OneOf.propertyName(message, oneOf),
                    Const.Message.OneOf.IOS.SERIALIZE_FUNCTION_NAME,
                    Const.Message.IOS.SerializeFunction.STREAM_PARAM
                )
            }
        }
    }

    private fun buildWrapperDeserializationFunction(builder: FunSpec.Builder, message: ProtoMessage) {
        val wrapperParamName = Const.Message.Companion.IOS.WrapperDeserializationFunction.WRAPPER_PARAM

        val getOneOfVarName = { oneOf: ProtoOneOf -> oneOf.name }

        val getAttrVarName = { attr: ProtoMessageAttribute ->
            when (attr.attributeType) {
                is MapType -> "${attr.name}Map"
                is Repeated -> "${attr.name}List"
                is Scalar -> attr.name
            }
        }

        builder.apply {
            message.attributes.filter { !it.isOneOfAttribute }.forEach { attr ->
                val (type, mutable) = when (attr.attributeType) {
                    is Scalar -> attr.types.iosType.copy(nullable = attr.types.isNullable) to true
                    is Repeated -> MUTABLE_LIST.parameterizedBy(attr.types.iosType) to false
                    is MapType -> MUTABLE_MAP.parameterizedBy(
                        attr.attributeType.keyTypes.iosType,
                        attr.attributeType.valueTypes.iosType
                    ) to false
                }

                addCode(
                    if (mutable) "var %N: %T = " else "val %N: %T = ",
                    getAttrVarName(attr),
                    type
                )
                addCode(attr.commonDefaultValue(true))
                addCode("\n")
            }

            message.oneOfs.forEach { oneOf ->
                addCode(
                    "var %N: %T = ",
                    getOneOfVarName(oneOf),
                    Const.Message.OneOf.parentSealedClassName(message, oneOf)
                )

                addCode(oneOf.defaultValue(message))
                addCode("\n")
            }

            beginControlFlow("while (true)")
            addStatement(
                "val tag = %N.stream.readTag()",
                wrapperParamName
            )
            addStatement("if (tag == 0) break")

            beginControlFlow("when (tag)")

            val getScalarAndRepeatedTagCode = { attr: ProtoMessageAttribute ->
                val isPacked = isAttrPackable(attr) && attr.attributeType is Repeated

                val gpbType = getGPBDataTypeForProtoType(attr.types.protoType)

                CodeBlock.of(
                    "%M(%L, %M(%M, %L)).toInt()",
                    GPBWireFormatMakeTag,
                    attr.protoId,
                    GPBWireFormatForType,
                    gpbType,
                    isPacked
                )
            }

            //The function name of that reads the type from the input stream
            val getScalarReadFunctionName = { protoType: ProtoType ->
                when (protoType) {
                    ProtoType.DOUBLE -> "readDouble"
                    ProtoType.FLOAT -> "readFloat"
                    ProtoType.INT_32 -> "readInt32"
                    ProtoType.INT_64 -> "readInt64"
                    ProtoType.BOOL -> "readBool"
                    ProtoType.STRING -> "readString"
                    ProtoType.ENUM, ProtoType.MAP, ProtoType.MESSAGE -> throw IllegalStateException()
                }
            }

            val getScalarReadFieldCode = { attr: ProtoMessageAttribute ->
                when (attr.types.protoType) {
                    ProtoType.DOUBLE,
                    ProtoType.FLOAT,
                    ProtoType.INT_32,
                    ProtoType.INT_64,
                    ProtoType.BOOL,
                    ProtoType.STRING -> {
                        CodeBlock.of(
                            "%N.stream.%N()",
                            wrapperParamName,
                            getScalarReadFunctionName(attr.types.protoType)
                        )
                    }

                    ProtoType.MESSAGE -> CodeBlock.of(
                        "%M(%N, %T.Companion::%N)",
                        readKMMessage,
                        wrapperParamName,
                        attr.types.iosType,
                        Const.Message.Companion.IOS.WrapperDeserializationFunction.NAME
                    )

                    ProtoType.ENUM -> CodeBlock.of(
                        "%T.%N(%N.stream.readEnum())",
                        attr.types.iosType,
                        Const.Enum.getEnumForNumFunctionName,
                        wrapperParamName
                    )

                    else -> throw IllegalStateException()
                }
            }

            message.attributes.filter { !it.isOneOfAttribute }.forEach { attr ->
                when (attr.attributeType) {
                    is Scalar, is Repeated -> {
                        addCode(getScalarAndRepeatedTagCode(attr))
                    }

                    is MapType -> addCode(
                        "%M(%L, %M(%M, false)).toInt()",
                        GPBWireFormatMakeTag,
                        attr.protoId,
                        GPBWireFormatForType,
                        MemberName(COCOAPODS_PROTOBUF_PACKAGE, "GPBDataTypeMessage")
                    )
                }

                addCode(" -> ")

                when (attr.attributeType) {
                    is Scalar -> {
                        addCode("%N·=·", getAttrVarName(attr))
                        addCode(getScalarReadFieldCode(attr))
                        addCode("\n")
                    }

                    is Repeated -> {
                        beginControlFlow("{")
                        when (attr.types.protoType) {
                            ProtoType.DOUBLE,
                            ProtoType.FLOAT,
                            ProtoType.INT_32,
                            ProtoType.INT_64,
                            ProtoType.BOOL,
                            ProtoType.ENUM -> {
                                //All packable
                                addStatement(
                                    "val length·=·%N.stream.readInt32().toULong()",
                                    wrapperParamName
                                )
                                addStatement(
                                    "val limit·=·%N.stream.pushLimit(length)",
                                    wrapperParamName
                                )
                                beginControlFlow(
                                    "while·(!%N.stream.isAtEnd())·{",
                                    wrapperParamName
                                )
                                //Enums need to first get mapped
                                if (attr.types.protoType == ProtoType.ENUM) {
                                    addStatement(
                                        "%N += %T.%N(%N.stream.readEnum())",
                                        getAttrVarName(attr),
                                        attr.types.commonType,
                                        Const.Enum.getEnumForNumFunctionName,
                                        wrapperParamName
                                    )
                                } else {
                                    val functionName = getScalarReadFunctionName(attr.types.protoType)

                                    addStatement(
                                        "%N·+=·%N.stream.%N()",
                                        getAttrVarName(attr),
                                        wrapperParamName,
                                        functionName
                                    )
                                }

                                endControlFlow()

                                addStatement("%N.stream.popLimit(limit)", wrapperParamName)
                            }

                            ProtoType.MESSAGE -> addCode(
                                "%N·+=·%M(%N, %T.Companion::%N)\n",
                                getAttrVarName(attr),
                                readKMMessage,
                                wrapperParamName,
                                attr.types.iosType,
                                Const.Message.Companion.IOS.WrapperDeserializationFunction.NAME
                            )

                            ProtoType.STRING -> addCode(
                                "%N·+=·%N.stream.readString()",
                                getAttrVarName(attr),
                                wrapperParamName
                            )

                            ProtoType.MAP -> throw IllegalStateException()
                        }
                        endControlFlow()
                    }

                    is MapType -> {
                        addCode(
                            "%M(%N, %N, %M, %M, ",
                            readMapEntry,
                            Const.Message.Companion.IOS.WrapperDeserializationFunction.WRAPPER_PARAM,
                            getAttrVarName(attr),
                            getGPBDataTypeForProtoType(attr.attributeType.keyTypes.protoType),
                            getGPBDataTypeForProtoType(attr.attributeType.valueTypes.protoType),
                        )

                        val getDefaultEntry = { types: Types ->
                            when (types.protoType) {
                                ProtoType.DOUBLE -> CodeBlock.of("0.0")
                                ProtoType.FLOAT -> CodeBlock.of("0f")
                                ProtoType.INT_32 -> CodeBlock.of("0")
                                ProtoType.INT_64 -> CodeBlock.of("0L")
                                ProtoType.BOOL -> CodeBlock.of("false")
                                ProtoType.STRING -> CodeBlock.of("\"\"")
                                ProtoType.MAP -> throw IllegalStateException()
                                ProtoType.MESSAGE -> CodeBlock.of("%T()", types.iosType)
                                ProtoType.ENUM -> CodeBlock.of(
                                    "%T.%N(0)",
                                    types.iosType,
                                    Const.Enum.getEnumForNumFunctionName
                                )
                            }
                        }

                        val addReadScalarCode = { types: Types ->
                            addCode(
                                "{·stream.%N()·}",
                                getScalarReadFunctionName(types.protoType)
                            )
                        }

                        val addReadEnumCode = { types: Types ->
                            addCode(
                                "{·%T.%N(stream.readEnum())·}",
                                types.iosType,
                                Const.Enum.getEnumForNumFunctionName
                            )
                        }

                        //Write default values
                        addCode(getDefaultEntry(attr.attributeType.keyTypes))
                        addCode(", ")
                        addCode(getDefaultEntry(attr.attributeType.valueTypes))
                        addCode(", ")

                        when (attr.attributeType.keyTypes.protoType) {
                            ProtoType.INT_32,
                            ProtoType.INT_64,
                            ProtoType.BOOL,
                            ProtoType.STRING -> addReadScalarCode(attr.attributeType.keyTypes)

                            ProtoType.ENUM,
                            ProtoType.MESSAGE,
                            ProtoType.MAP,
                            ProtoType.DOUBLE,
                            ProtoType.FLOAT -> throw IllegalStateException("Illegal key types")
                        }
                        addCode(", ")
                        when (attr.attributeType.valueTypes.protoType) {
                            ProtoType.DOUBLE,
                            ProtoType.FLOAT,
                            ProtoType.INT_32,
                            ProtoType.INT_64,
                            ProtoType.BOOL,
                            ProtoType.STRING -> addReadScalarCode(attr.attributeType.valueTypes)

                            ProtoType.MESSAGE -> addCode(
                                "{·%M(this, %T.Companion::%N)}",
                                readKMMessage,
                                attr.attributeType.valueTypes.iosType,
                                Const.Message.Companion.IOS.WrapperDeserializationFunction.NAME
                            )

                            ProtoType.ENUM -> addReadEnumCode(attr.attributeType.valueTypes)
                            ProtoType.MAP -> throw IllegalStateException()
                        }
                        addCode(")\n")
                    }
                }
            }

            message.oneOfs.forEach { protoOneOf ->
                protoOneOf.attributes.forEach { attr ->
                    addCode(getScalarAndRepeatedTagCode(attr))
                    addCode(
                        "·->·%N·=·%T(",
                        getOneOfVarName(protoOneOf),
                        Const.Message.OneOf.childClassName(message, protoOneOf, attr)
                    )
                    addCode(getScalarReadFieldCode(attr))
                    addCode(")\n")
                }
            }

            endControlFlow()

            endControlFlow()

            addCode("return %T(", message.iosType)
            message.attributes.filter { !it.isOneOfAttribute }.forEach { attr ->
                addCode("%N·=·%N, ", Const.Message.Attribute.propertyName(message, attr), getAttrVarName(attr))
            }

            message.oneOfs.forEach { oneOf ->
                addCode("%N·=·%N, ", Const.Message.OneOf.propertyName(message, oneOf), getOneOfVarName(oneOf))
            }

            addCode(")\n")
        }
    }

    private fun isAttrPackable(attr: ProtoMessageAttribute) = when (attr.types.protoType) {
        ProtoType.DOUBLE,
        ProtoType.FLOAT,
        ProtoType.INT_32,
        ProtoType.INT_64,
        ProtoType.BOOL,
        ProtoType.ENUM -> true
        ProtoType.MESSAGE, ProtoType.STRING -> false
        ProtoType.MAP -> throw IllegalStateException()
    }

    private fun getGPBDataTypeForProtoType(protoType: ProtoType) =
        MemberName(
            COCOAPODS_PROTOBUF_PACKAGE,
            when (protoType) {
                ProtoType.DOUBLE -> "GPBDataTypeDouble"
                ProtoType.FLOAT -> "GPBDataTypeFloat"
                ProtoType.INT_32 -> "GPBDataTypeInt32"
                ProtoType.INT_64 -> "GPBDataTypeInt64"
                ProtoType.BOOL -> "GPBDataTypeBool"
                ProtoType.STRING -> "GPBDataTypeString"
                ProtoType.MESSAGE -> "GPBDataTypeMessage"
                ProtoType.ENUM -> "GPBDataTypeEnum"
                ProtoType.MAP -> throw IllegalStateException()
            }
        )


    private fun getComputeMapValueRequiredSizeCode(valueTypes: Types): CodeBlock {
        return when (valueTypes.protoType) {
            ProtoType.DOUBLE,
            ProtoType.FLOAT,
            ProtoType.INT_32,
            ProtoType.INT_64,
            ProtoType.BOOL,
            ProtoType.STRING, ProtoType.MESSAGE -> CodeBlock.of(
                "::%M",
                getComputeDataTypeSizeMember(valueTypes.protoType, true)
            )

            ProtoType.ENUM -> CodeBlock.of(
                "{·fieldNumber,·it·-> %M(fieldNumber, it.%N)·}",
                getComputeDataTypeSizeMember(valueTypes.protoType, true),
                Const.Enum.VALUE_PROPERTY_NAME
            )

            ProtoType.MAP -> throw IllegalStateException()
        }
    }

    companion object {
        /**
         * @return the function that compute the size of the data type.
         */
        private fun getComputeDataTypeSizeMember(protoType: ProtoType, withTag: Boolean): MemberName {
            return when (protoType) {
                ProtoType.DOUBLE,
                ProtoType.FLOAT,
                ProtoType.INT_32,
                ProtoType.INT_64,
                ProtoType.BOOL,
                ProtoType.STRING,
                ProtoType.ENUM -> {
                    val name = when (protoType) {
                        ProtoType.DOUBLE -> "Double"
                        ProtoType.FLOAT -> "Float"
                        ProtoType.INT_32 -> "Int32"
                        ProtoType.INT_64 -> "Int64"
                        ProtoType.BOOL -> "Bool"
                        ProtoType.STRING -> "String"
                        ProtoType.ENUM -> "Enum"
                        ProtoType.MAP, ProtoType.MESSAGE -> throw IllegalStateException()
                    }
                    MemberName("cocoapods.Protobuf", "GPBCompute${name}Size${if (!withTag) "NoTag" else ""}")
                }

                ProtoType.MESSAGE -> computeMessageSize
                ProtoType.MAP -> computeMapSize
            }
        }

        fun getCodeForRequiredSizeForScalarAttributeC(attr: ProtoMessageAttribute): CodeBlock =
            when (attr.types.protoType) {
                ProtoType.DOUBLE,
                ProtoType.FLOAT,
                ProtoType.INT_32,
                ProtoType.INT_64,
                ProtoType.BOOL,
                ProtoType.STRING -> {
                    CodeBlock.of(
                        "%M(%L, %N)",
                        getComputeDataTypeSizeMember(attr.types.protoType, true),
                        attr.protoId,
                        attr.name
                    )
                }

                ProtoType.MESSAGE -> CodeBlock.of(
                    "%M(%L, %N)",
                    MemberName(
                        "io.github.timortel.kotlin_multiplatform_grpc_lib.message",
                        "computeMessageSize"
                    ),
                    attr.protoId,
                    attr.name
                )

                ProtoType.ENUM -> CodeBlock.of(
                    "%M(%L, %N.%N)",
                    MemberName("cocoapods.Protobuf", "GPBComputeEnumSize"),
                    attr.protoId,
                    attr.name,
                    Const.Enum.VALUE_PROPERTY_NAME
                )

                ProtoType.MAP -> throw IllegalStateException()
            }

        fun getWriteScalarFieldCode(
            message: ProtoMessage,
            attr: ProtoMessageAttribute,
            streamParam: String
        ): CodeBlock =
            when (attr.types.protoType) {
                ProtoType.DOUBLE,
                ProtoType.FLOAT,
                ProtoType.INT_32,
                ProtoType.INT_64,
                ProtoType.BOOL,
                ProtoType.STRING -> {
                    val functionName = getWriteScalarFunctionName(attr.types.protoType)

                    CodeBlock.of(
                        "%N.%N(%L, %N)\n",
                        streamParam,
                        functionName,
                        attr.protoId,
                        attr.name
                    )
                }

                ProtoType.MESSAGE -> {
                    CodeBlock.builder().apply {
                        beginControlFlow(
                            "if (%N)",
                            Const.Message.Attribute.Scalar.IOS.isMessageSetFunctionName(message, attr)
                        )

                        addStatement(
                            "%M(%N, %L, %N)",
                            MemberName("io.github.timortel.kotlin_multiplatform_grpc_lib.io", "writeKMMessage"),
                            streamParam,
                            attr.protoId,
                            attr.name
                        )

                        endControlFlow()
                    }.build()
                }

                ProtoType.ENUM -> {
                    CodeBlock.of(
                        "%N.writeEnum(%L, %N.%N)\n",
                        streamParam,
                        attr.protoId,
                        attr.name,
                        Const.Enum.VALUE_PROPERTY_NAME
                    )
                }

                ProtoType.MAP -> throw IllegalStateException()
            }

        private fun getWriteScalarFunctionName(protoType: ProtoType) =
            when (protoType) {
                ProtoType.DOUBLE -> "writeDouble"
                ProtoType.FLOAT -> "writeFloat"
                ProtoType.INT_32 -> "writeInt32"
                ProtoType.INT_64 -> "writeInt64"
                ProtoType.BOOL -> "writeBool"
                ProtoType.STRING -> "writeString"
                ProtoType.ENUM -> "writeEnum"
                ProtoType.MAP, ProtoType.MESSAGE -> throw IllegalStateException()
            }
    }

    override fun getChildClassName(parentClass: ClassName?, childName: String): ClassName =
        getChildClassName(parentClass, childName, protoFile.pkg)
}