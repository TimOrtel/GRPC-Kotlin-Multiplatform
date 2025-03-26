package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model

import com.squareup.kotlinpoet.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.Protobuf3CompilationException
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.ProtoDeclaration
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.ProtoEnum
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.field.ProtoMapField
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.field.ProtoMessageField
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.field.ProtoOneOfField
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.service.ProtoRpc
import org.antlr.v4.runtime.ParserRuleContext

sealed interface ProtoType {

    var parent: Parent

    val file: ProtoFile get() = parent.file

    val isPackable: Boolean

    val isMessage: Boolean
    val isEnum: Boolean

    fun resolve(): TypeName

    /**
     * @return the default value for this type as a code block
     */
    fun defaultValue(messageDefaultValue: MessageDefaultValue = MessageDefaultValue.NULL): CodeBlock

    enum class MessageDefaultValue {
        NULL,
        EMPTY
    }

    sealed class NonDeclType(val type: ClassName, private val defaultValue: CodeBlock) : ProtoType {

        override lateinit var parent: Parent

        override val isPackable: Boolean get() = this != StringType

        override val isMessage: Boolean = false
        override val isEnum: Boolean = false

        override fun resolve(): TypeName = type

        override fun defaultValue(messageDefaultValue: MessageDefaultValue): CodeBlock = defaultValue
    }

    sealed class MapKeyType(type: ClassName, defaultValue: CodeBlock) : NonDeclType(type, defaultValue)

    data object DoubleType : NonDeclType(DOUBLE, CodeBlock.of("0.0"))
    data object FloatType : NonDeclType(FLOAT, CodeBlock.of("0.0f"))
    data object Int32Type : MapKeyType(INT, CodeBlock.of("0"))
    data object Int64Type : MapKeyType(LONG, CodeBlock.of("0L"))
    data object UInt32Type : MapKeyType(U_INT, CodeBlock.of("0"))
    data object UInt64Type : MapKeyType(U_LONG, CodeBlock.of("0L"))
    data object SInt32Type : MapKeyType(INT, CodeBlock.of("0"))
    data object SInt64Type : MapKeyType(LONG, CodeBlock.of("0L"))
    data object Fixed32Type : MapKeyType(U_INT, CodeBlock.of("0"))
    data object Fixed64Type : MapKeyType(U_LONG, CodeBlock.of("0L"))
    data object SFixed32Type : MapKeyType(INT, CodeBlock.of("0"))
    data object SFixed64Type : MapKeyType(LONG, CodeBlock.of("0L"))
    data object BoolType : MapKeyType(BOOLEAN, CodeBlock.of("false"))
    data object StringType : MapKeyType(STRING, CodeBlock.of("\"\""))
    data object BytesType : NonDeclType(BYTE_ARRAY, CodeBlock.of("emptyArray<Byte>()"))


    /**
     * Message or Enum Types
     */
    data class DefType(val declaration: String, val ctx: ParserRuleContext) : ProtoType {
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

        private fun resolveDeclaration(): ProtoDeclaration {
            val decl = if (declaration.startsWith('.')) {
                val fullyQualifiedName = declaration.substring(1)

                parent.project.resolveDeclarationFullyQualified(copy(declaration = fullyQualifiedName))
            } else when (val p = parent) {
                is Parent.MessageField -> p.field.parent.resolveDeclaration(this)
                is Parent.MapField -> p.field.message.resolveDeclaration(this)
                is Parent.OneOfField -> p.field.parent.message.resolveDeclaration(this)
                is Parent.Rpc -> p.rpc.file.resolveDeclaration(this)
            }

            if (decl == null) {
                throw Protobuf3CompilationException("Unresolved reference $declaration", file, ctx)
            }

            return decl
        }

        fun copy(declaration: String): DefType {
            val new = DefType(declaration, ctx)
            new.parent = parent
            return new
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
    }
}
