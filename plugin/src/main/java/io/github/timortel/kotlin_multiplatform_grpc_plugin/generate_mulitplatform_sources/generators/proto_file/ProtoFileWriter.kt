package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.proto_file

import com.squareup.kotlinpoet.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.MapMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof.OneOfMethodAndClassGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.repeated.RepeatedMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.scalar.ScalarMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.unrecognizedEnumField
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.kmMessage
import java.io.File

abstract class ProtoFileWriter(private val protoFile: ProtoFile, private val isActual: Boolean) {

    abstract val scalarMessageMethodGenerator: ScalarMessageMethodGenerator

    abstract val repeatedMessageMethodGenerator: RepeatedMessageMethodGenerator

    abstract val oneOfMethodAndClassGenerator: OneOfMethodAndClassGenerator

    abstract val mapMessageMethodGenerator: MapMessageMethodGenerator

    open val additionalSuperinterfaces: List<TypeName> = emptyList()

    open fun writeFile(outputDir: File) {
        protoFile.messages.forEach { message ->
            FileSpec
                .builder(protoFile.pkg, message.commonName)
                .addType(
                    generateProtoMessageClass(
                        null,
                        message
                    )

                )
                .build()
                .writeTo(outputDir)
        }
    }

    /**
     * Recursive function that adds a proto message class.
     */
    private fun generateProtoMessageClass(parentClass: ClassName?, message: ProtoMessage): TypeSpec {
        val isNested = parentClass != null
        val messageClassName = getChildClassName(parentClass, message.commonName)

        return (
                if (isActual) TypeSpec.classBuilder(message.commonName).apply {
                    addModifiers(KModifier.ACTUAL)
                } else if (!isNested) TypeSpec
                    .expectClassBuilder(message.commonName)
                else TypeSpec.classBuilder(message.commonName)
                )
            .apply {
                addSuperinterface(kmMessage)
                addSuperinterfaces(additionalSuperinterfaces)

                message.attributes.forEach { attr ->
                    //We change nothing here
                    when (attr.attributeType) {
                        is Scalar -> addSimpleMessageFunctions(this, message, attr)
                        is Repeated -> addRepeatedMessageFunctions(this, message, attr)
                        is MapType -> mapMessageMethodGenerator.generateFunctions(this, message, attr)
                    }
                }

                message.oneOfs.forEach { oneOf ->
                    addOneOfEnumAndFunctions(this, message, oneOf)
                }

                applyToClass(this, message, messageClassName)

                //Write child messages
                message.children.forEach { childMessage ->
                    addType(
                        generateProtoMessageClass(
                            messageClassName,
                            childMessage
                        )
                    )
                }

                //Write child enums
                message.enums.forEach { childEnum ->
                    addProtoEnum(
                        this::addType,
                        if (isActual) EnumType.ACTUAL else EnumType.EXPECTED,
                        childEnum
                    ) { childName -> messageClassName.nestedClass(childName) }
                }

                //Write eq function
                addFunction(
                    FunSpec.builder(Const.Message.BasicFunctions.EqualsFunction.NAME)
                        .addModifiers(if (isActual) KModifier.ACTUAL else KModifier.EXPECT, KModifier.OVERRIDE)
                        .addParameter(
                            Const.Message.BasicFunctions.EqualsFunction.OTHER_PARAM,
                            ANY.copy(nullable = true)
                        )
                        .returns(BOOLEAN)
                        .apply {
                            applyToEqualsFunction(this, message, messageClassName)
                        }
                        .build()
                )

                addFunction(
                    FunSpec.builder(Const.Message.BasicFunctions.HashCodeFunction.NAME)
                        .returns(INT)
                        .addModifiers(if (isActual) KModifier.ACTUAL else KModifier.EXPECT, KModifier.OVERRIDE)
                        .apply {
                            applyToHashCodeFunction(this, message)
                        }
                        .build()
                )
            }
            .build()
    }

    abstract fun applyToClass(builder: TypeSpec.Builder, message: ProtoMessage, messageClassName: ClassName)

    /**
     * @param thisClassName the ClassName of the class we are constructing right now.
     */
    open fun applyToEqualsFunction(
        builder: FunSpec.Builder,
        message: ProtoMessage,
        thisClassName: ClassName
    ) {
        if (isActual) {
            builder.apply {
                val otherParamName = Const.Message.BasicFunctions.EqualsFunction.OTHER_PARAM

                addStatement("if (%N === this) return true", otherParamName)
                addStatement("if (%N !is %T) return false", otherParamName, thisClassName)

                message.attributes.filter { !it.isOneOfAttribute }.forEach { attr ->
                    when (attr.attributeType) {
                        is Scalar -> {
                            addStatement(
                                "if (%1N != %2N.%1N) return false",
                                attr.name,
                                otherParamName
                            )
                        }

                        is Repeated -> addStatement(
                            "if (%1N != %2N.%1N) return false",
                            Const.Message.Attribute.Repeated.listPropertyName(attr),
                            otherParamName
                        )

                        is MapType -> addStatement(
                            "if (%1N != %2N.%1N) return false",
                            Const.Message.Attribute.Map.propertyName(attr),
                            otherParamName
                        )
                    }
                }

                //Assume that each one of sealed class has their equals method set properly
                message.oneOfs.forEach { oneOf ->
                    addStatement(
                        "if (%1N != %2N.%1N) return false",
                        Const.Message.OneOf.propertyName(message, oneOf),
                        otherParamName
                    )
                }

                addStatement("return true")
            }
        }
    }

    private sealed class Property {
        abstract fun propertyName(message: ProtoMessage): String

        class Attribute(val attr: ProtoMessageAttribute) : Property() {
            override fun propertyName(message: ProtoMessage): String {
                return Const.Message.Attribute.propertyName(message, attr)
            }
        }

        class OneOf(val oneOf: ProtoOneOf) : Property() {
            override fun propertyName(message: ProtoMessage): String {
                return Const.Message.OneOf.propertyName(message, oneOf)
            }
        }
    }

    open fun applyToHashCodeFunction(builder: FunSpec.Builder, message: ProtoMessage) {
        if (isActual) {
            val properties =
                message.attributes.filter { !it.isOneOfAttribute }.map { Property.Attribute(it) } +
                        message.oneOfs.map { Property.OneOf(it) }

            builder.apply {
                if (properties.isEmpty()) {
                    addStatement("return 0")
                    return
                }

                if (properties.size == 1) {
                    addStatement("return %N.hashCode()", properties.first().propertyName(message))
                    return
                }

                properties.forEachIndexed { index, property ->
                    val attrName = property.propertyName(message)

                    //Mimic the way IntelliJ generates hashCode
                    if (index == 0) {
                        addStatement("var result = %N.hashCode()", attrName)
                    } else {
                        addStatement("result = 31 * result + %N.hashCode()", attrName)
                    }
                }

                addStatement("return result")
            }
        }
    }

    private fun addSimpleMessageFunctions(
        builder: TypeSpec.Builder,
        message: ProtoMessage,
        attr: ProtoMessageAttribute
    ) {
        scalarMessageMethodGenerator.generateProperties(builder, message, attr)
    }

    private fun addRepeatedMessageFunctions(
        builder: TypeSpec.Builder,
        message: ProtoMessage,
        attr: ProtoMessageAttribute
    ) {
        repeatedMessageMethodGenerator.generateFunctions(builder, message, attr)
    }

    private fun addOneOfEnumAndFunctions(builder: TypeSpec.Builder, message: ProtoMessage, oneOf: ProtoOneOf) {
        oneOfMethodAndClassGenerator.generateMethodsAndClasses(builder, message, oneOf)
    }

    /**
     * @param parentClass the parent class, or null if this is a top level class.
     * @return the ClassName of the class. Should consider recursion, meaning that this child class could also be the child of another child class.
     */
    abstract fun getChildClassName(parentClass: ClassName?, childName: String): ClassName

    /**
     * @param getChildClassName a function that will return a child class name for the parent. So when the parent is a Class then
     * the function should return a nested class.
     */
    protected fun addProtoEnum(
        addType: (TypeSpec) -> Unit,
        enumType: EnumType,
        protoEnum: ProtoEnum,
        getChildClassName: (childName: String) -> ClassName
    ) {
        /*
         * Top level enums do not need actual and expect declarations, while nested enums do need them
         */
        val supplyImplementation = enumType == EnumType.TOP_LEVEL || enumType == EnumType.ACTUAL

        val enumClassName = getChildClassName(Const.Enum.commonEnumName(protoEnum))
        addType(
            TypeSpec
                .enumBuilder(Const.Enum.commonEnumName(protoEnum))
                .addProperty(
                    PropertySpec
                        .builder(Const.Enum.VALUE_PROPERTY_NAME, Int::class)
                        .apply {
                            if (supplyImplementation) initializer(Const.Enum.VALUE_PROPERTY_NAME)
                            if (enumType == EnumType.ACTUAL) addModifiers(KModifier.ACTUAL)
                        }
                        .build()
                )
                .apply {
                    if (supplyImplementation) {
                        primaryConstructor(
                            FunSpec
                                .constructorBuilder()
                                .addParameter(Const.Enum.VALUE_PROPERTY_NAME, Int::class)
                                .build()
                        )

                        if (enumType == EnumType.ACTUAL) addModifiers(KModifier.ACTUAL)
                    }

                    protoEnum.fields.forEach { enumField ->
                        addEnumConstant(
                            enumField.name,
                            TypeSpec
                                .anonymousClassBuilder()
                                .apply {
                                    if (supplyImplementation) {
                                        addSuperclassConstructorParameter("%L", enumField.num)
                                    }
                                }
                                .build()
                        )
                    }

                    //unrecognizedEnumField
                    addEnumConstant(
                        unrecognizedEnumField, TypeSpec
                            .anonymousClassBuilder()
                            .apply {
                                if (supplyImplementation) {
                                    addSuperclassConstructorParameter("-1")
                                }
                            }
                            .build()
                    )
                }
                //The method that will return the correct enum for the given num
                .addType(
                    TypeSpec
                        .companionObjectBuilder()
                        .apply {
                            if (enumType == EnumType.ACTUAL) addModifiers(KModifier.ACTUAL)
                        }
                        .addFunction(
                            FunSpec
                                .builder(Const.Enum.getEnumForNumFunctionName)
                                .returns(enumClassName)
                                .addParameter("num", Int::class)
                                .apply {
                                    if (supplyImplementation) {
                                        addCode(
                                            CodeBlock
                                                .builder()
                                                .add("return ")
                                                .beginControlFlow("when(num)")
                                                .apply {
                                                    protoEnum.fields.forEach { field ->
                                                        add("%L -> %N\n", field.num, field.name)
                                                    }
                                                    add("else -> %N\n", unrecognizedEnumField)
                                                }
                                                .endControlFlow()
                                                .build()
                                        )

                                        if (enumType == EnumType.ACTUAL) {
                                            addModifiers(KModifier.ACTUAL)
                                        }
                                    }
                                }
                                .build()
                        )
                        .build()
                )
                .build()
        )
    }

    protected enum class EnumType {
        TOP_LEVEL,
        EXPECTED,
        ACTUAL
    }
}