package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type

import com.squareup.kotlinpoet.*
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.CompilationException
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.*
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoExtensionDefinition
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoProject
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoDeclaration
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoEnum
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMapField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMessageField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoOneOfField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.service.ProtoRpc
import io.github.timortel.kmpgrpc.shared.internal.io.DataType
import org.antlr.v4.runtime.ParserRuleContext

/**
 * General representation of a type in the proto language, for example bool, message, enum, etc.
 */
sealed interface ProtoType {

    var parent: Parent

    val file: ProtoFile get() = parent.file

    /**
     * If this type can be written in packed form
     */
    val isPackable: Boolean

    /**
     * If this represents a message
     */
    val isMessage: Boolean

    /**
     * If this represents an enum
     */
    val isEnum: Boolean

    /**
     * The proto wire type for this type. E.g. "MESSAGE", "BOOL", etc.
     */
    val wireType: DataType

    /**
     * The field type of this message, as defined in the core.
     */
    val fieldType: TypeName

    fun resolve(): TypeName

    /**
     * @return the default value for this type as a code block
     */
    fun defaultValue(messageDefaultValue: MessageDefaultValue = MessageDefaultValue.NULL): CodeBlock

    fun inequalityCode(attributeName: String, otherParamName: String, isRepeated: Boolean): CodeBlock

    /**
     * @return code block with boolean condition evaluating to true of the given variable contains the default value of this type
     */
    fun isDefaultValueCode(
        attributeName: String,
        isRepeated: Boolean,
        messageDefaultValue: MessageDefaultValue = MessageDefaultValue.NULL
    ): CodeBlock

    /**
     * @return code block with boolean condition evaluating to true of the given variable does not contain the default value of this type
     */
    fun isNotDefaultValueCode(
        attributeName: String,
        isRepeated: Boolean,
        messageDefaultValue: MessageDefaultValue = MessageDefaultValue.NULL
    ): CodeBlock

    enum class MessageDefaultValue {
        NULL,
        EMPTY
    }

    sealed class NonDeclType(
        val type: ClassName,
        private val defaultValue: CodeBlock,
        override val wireType: DataType,
        override val fieldType: TypeName
    ) : ProtoType {

        override lateinit var parent: Parent

        override val isPackable: Boolean get() = this != StringType && this != BytesType

        override val isMessage: Boolean = false
        override val isEnum: Boolean = false

        override fun resolve(): TypeName = type

        override fun defaultValue(messageDefaultValue: MessageDefaultValue): CodeBlock = defaultValue
    }

    sealed class MapKeyType(type: ClassName, defaultValue: CodeBlock, wireType: DataType, fieldType: TypeName) :
        NonDeclType(type, defaultValue, wireType, fieldType)

    data object DoubleType : NonDeclType(DOUBLE, CodeBlock.of("0.0"), DataType.DOUBLE, fieldTypeDouble), DefaultInequalityProvider
    data object FloatType : NonDeclType(FLOAT, CodeBlock.of("0.0f"), DataType.FLOAT, fieldTypeFloat), DefaultInequalityProvider
    data object Int32Type : MapKeyType(INT, CodeBlock.of("0"), DataType.INT32, fieldTypeInt32), DefaultInequalityProvider
    data object Int64Type : MapKeyType(LONG, CodeBlock.of("0L"), DataType.INT64, fieldTypeInt64), DefaultInequalityProvider
    data object UInt32Type : MapKeyType(U_INT, CodeBlock.of("0u"), DataType.UINT32, fieldTypeUInt32), DefaultInequalityProvider
    data object UInt64Type : MapKeyType(U_LONG, CodeBlock.of("0uL"), DataType.UINT64, fieldTypeUInt64), DefaultInequalityProvider
    data object SInt32Type : MapKeyType(INT, CodeBlock.of("0"), DataType.SINT32, fieldTypeSInt32), DefaultInequalityProvider
    data object SInt64Type : MapKeyType(LONG, CodeBlock.of("0L"), DataType.SINT64, fieldTypeSInt64), DefaultInequalityProvider
    data object Fixed32Type : MapKeyType(U_INT, CodeBlock.of("0u"), DataType.FIXED32, fieldTypeFixed32), DefaultInequalityProvider
    data object Fixed64Type : MapKeyType(U_LONG, CodeBlock.of("0uL"), DataType.FIXED64, fieldTypeFixed64), DefaultInequalityProvider
    data object SFixed32Type : MapKeyType(INT, CodeBlock.of("0"), DataType.SFIXED32, fieldTypeSFixed32), DefaultInequalityProvider
    data object SFixed64Type : MapKeyType(LONG, CodeBlock.of("0L"), DataType.SFIXED64, fieldTypeSFixed64), DefaultInequalityProvider
    data object StringType : MapKeyType(STRING, CodeBlock.of("\"\""), DataType.STRING, fieldTypeString), DefaultInequalityProvider

    data object BoolType : MapKeyType(BOOLEAN, CodeBlock.of("false"), DataType.BOOL, fieldTypeBool), DefaultInequalityProvider {
        override fun isDefaultValueCode(
            attributeName: String,
            isRepeated: Boolean,
            messageDefaultValue: MessageDefaultValue
        ): CodeBlock {
            return if (isRepeated) {
                super.isDefaultValueCode(attributeName, true, messageDefaultValue)
            } else {
                CodeBlock.of("!%N", attributeName)
            }
        }

        override fun isNotDefaultValueCode(
            attributeName: String,
            isRepeated: Boolean,
            messageDefaultValue: MessageDefaultValue
        ): CodeBlock {
            return if (isRepeated) {
                super.isNotDefaultValueCode(attributeName, true, messageDefaultValue)
            } else {
                CodeBlock.of("%N", attributeName)
            }
        }
    }

    data object BytesType : NonDeclType(BYTE_ARRAY, CodeBlock.of("byteArrayOf()"), DataType.BYTES, fieldTypeBytes) {
        override fun inequalityCode(attributeName: String, otherParamName: String, isRepeated: Boolean): CodeBlock {
            return if (isRepeated) {
                CodeBlock.of("!%1M(%2N, %3N.%2N)", byteArrayListEquals, attributeName, otherParamName)
            } else {
                CodeBlock.of("!%N.contentEquals(%N.%N)", attributeName, otherParamName, attributeName)
            }
        }

        override fun isDefaultValueCode(
            attributeName: String,
            isRepeated: Boolean,
            messageDefaultValue: MessageDefaultValue
        ): CodeBlock {
            // IsEmpty both applicable for repeated case (List#isEmpty) and single (ByteArray#isEmpty)
            return CodeBlock.of("%N.isEmpty()", attributeName)
        }

        override fun isNotDefaultValueCode(
            attributeName: String,
            isRepeated: Boolean,
            messageDefaultValue: MessageDefaultValue
        ): CodeBlock {
            // IsEmpty both applicable for repeated case (List#isNotEmpty) and single (ByteArray#isNotEmpty)
            return CodeBlock.of("%N.isNotEmpty()", attributeName)
        }
    }


    /**
     * Message or Enum Types
     */
    data class DefType(val declaration: String, val ctx: ParserRuleContext) : ProtoType, DefaultInequalityProvider {
        override lateinit var parent: Parent

        val declType: DeclarationType
            get() = when (resolveDeclaration()) {
                is ProtoMessage -> DeclarationType.MESSAGE
                is ProtoEnum -> DeclarationType.ENUM
            }

        override val isPackable: Boolean
            get() = declType.isPackable

        override val isMessage: Boolean get() = declType == DeclarationType.MESSAGE
        override val isEnum: Boolean get() = declType == DeclarationType.ENUM

        override val wireType: DataType
            get() = when (declType) {
                DeclarationType.MESSAGE -> DataType.MESSAGE
                DeclarationType.ENUM -> DataType.ENUM
            }

        override val fieldType: TypeName
            get() = when (declType) {
                DeclarationType.MESSAGE -> fieldTypeMessage
                DeclarationType.ENUM -> fieldTypeEnum
            }

        override fun defaultValue(messageDefaultValue: MessageDefaultValue): CodeBlock {
            return when (val decl = resolveDeclaration()) {
                is ProtoEnum -> {
                    val defaultField = decl.defaultField
                    CodeBlock.of("%T.%M", decl.className, defaultField.memberName)
                }

                is ProtoMessage -> {
                    when (messageDefaultValue) {
                        MessageDefaultValue.NULL -> CodeBlock.of("null")
                        MessageDefaultValue.EMPTY -> CodeBlock.of("%T()", decl.className)
                    }
                }
            }
        }

        override fun resolve(): TypeName {
            return resolveDeclaration().className
        }

        fun resolveDeclaration(): ProtoDeclaration {
            val decl = if (declaration.startsWith('.')) {
                val fullyQualifiedName = declaration.substring(1)

                parent.project.resolveDeclarationFullyQualified(copy(declaration = fullyQualifiedName))
            } else when (val p = parent) {
                is Parent.MessageField -> p.field.declarationResolver.resolveDeclaration(this)
                is Parent.MapField -> p.field.declarationResolver.resolveDeclaration(this)
                is Parent.OneOfField -> p.field.parent.declarationResolver.resolveDeclaration(this)
                is Parent.Rpc -> p.rpc.file.resolveDeclaration(this)
                is Parent.ExtensionDefinition -> when (val ep = p.extensionDefinition.parent) {
                    is ProtoExtensionDefinition.Parent.File -> ep.file.resolveDeclaration(this)
                    is ProtoExtensionDefinition.Parent.Message -> ep.message.resolveDeclaration(this)
                }
            }

            if (decl == null) {
                throw CompilationException.UnresolvedReference("Unresolved reference $declaration", file, ctx)
            }

            return decl
        }

        fun copy(declaration: String): DefType {
            val new = DefType(declaration, ctx)
            new.parent = parent
            return new
        }

        override fun isDefaultValueCode(
            attributeName: String,
            isRepeated: Boolean,
            messageDefaultValue: MessageDefaultValue
        ): CodeBlock {
            return if (isRepeated) {
                super.isDefaultValueCode(attributeName, true, messageDefaultValue)
            } else when (val decl = resolveDeclaration()) {
                is ProtoMessage -> {
                    when (messageDefaultValue) {
                        MessageDefaultValue.NULL -> CodeBlock.of("%N == null", attributeName)
                        MessageDefaultValue.EMPTY -> CodeBlock.of("%N == %T()", attributeName, decl.className)
                    }
                }

                is ProtoEnum -> super.isDefaultValueCode(attributeName, false, messageDefaultValue)
            }
        }

        override fun isNotDefaultValueCode(
            attributeName: String,
            isRepeated: Boolean,
            messageDefaultValue: MessageDefaultValue
        ): CodeBlock {
            return if (isRepeated) {
                super.isNotDefaultValueCode(attributeName, true, messageDefaultValue)
            } else when (val decl = resolveDeclaration()) {
                is ProtoMessage -> {
                    when (messageDefaultValue) {
                        MessageDefaultValue.NULL -> CodeBlock.of("%N != null", attributeName)
                        MessageDefaultValue.EMPTY -> CodeBlock.of("%N != %T()", attributeName, decl.className)
                    }
                }

                is ProtoEnum -> super.isNotDefaultValueCode(attributeName, false, messageDefaultValue)
            }
        }

        enum class DeclarationType(val isPackable: Boolean) {
            MESSAGE(false),
            ENUM(true)
        }
    }

    sealed interface Parent {

        val file: ProtoFile
        val project: ProtoProject get() = file.project

        data class MessageField(val field: ProtoMessageField) : Parent {
            override val file: ProtoFile
                get() = this.field.file
        }

        data class MapField(val field: ProtoMapField) : Parent {
            override val file: ProtoFile
                get() = this.field.file
        }

        data class OneOfField(val field: ProtoOneOfField) : Parent {
            override val file: ProtoFile
                get() = this.field.file
        }

        data class Rpc(val rpc: ProtoRpc) : Parent {
            override val file: ProtoFile
                get() = rpc.file
        }

        data class ExtensionDefinition(val extensionDefinition: ProtoExtensionDefinition) : Parent {
            override val file: ProtoFile
                get() = extensionDefinition.file
        }
    }

    sealed interface DefaultInequalityProvider : ProtoType {
        override fun inequalityCode(attributeName: String, otherParamName: String, isRepeated: Boolean): CodeBlock {
            return CodeBlock.of("%N != %N.%N", attributeName, otherParamName, attributeName)
        }

        override fun isDefaultValueCode(
            attributeName: String,
            isRepeated: Boolean,
            messageDefaultValue: MessageDefaultValue
        ): CodeBlock {
            return if (isRepeated) {
                CodeBlock.of("%N.isEmpty()", attributeName)
            } else {
                CodeBlock.builder()
                    .add("%N == ", attributeName)
                    .add(defaultValue(messageDefaultValue))
                    .build()
            }
        }

        override fun isNotDefaultValueCode(
            attributeName: String,
            isRepeated: Boolean,
            messageDefaultValue: MessageDefaultValue
        ): CodeBlock {
            return if (isRepeated) {
                CodeBlock.of("%N.isNotEmpty()", attributeName)
            } else {
                CodeBlock.builder()
                    .add("%N != ", attributeName)
                    .add(defaultValue(messageDefaultValue))
                    .build()
            }
        }
    }
}
