package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model

import com.squareup.kotlinpoet.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.Protobuf3CompilationException
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.field.ProtoMapField
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.field.ProtoMessageField
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.field.ProtoOneOfField
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.service.ProtoRpc
import org.antlr.v4.runtime.ParserRuleContext

sealed interface ProtoType {

    var parent: Parent

    val file: ProtoFile get() = parent.file

    fun resolve(): TypeName

    sealed interface MapKeyType : ProtoType

    sealed class NonDeclType(val type: ClassName) : ProtoType {

        override lateinit var parent: Parent

        override fun resolve(): TypeName = type
    }

    data object DoubleType : NonDeclType(DOUBLE)
    data object FloatType : NonDeclType(FLOAT)
    data object Int32Type : NonDeclType(INT), MapKeyType
    data object Int64Type : NonDeclType(LONG), MapKeyType
    data object UInt32Type : NonDeclType(U_INT), MapKeyType
    data object UInt64Type : NonDeclType(U_LONG), MapKeyType
    data object SInt32Type : NonDeclType(INT), MapKeyType
    data object SInt64Type : NonDeclType(LONG), MapKeyType
    data object Fixed32Type : NonDeclType(U_INT), MapKeyType
    data object Fixed64Type : NonDeclType(U_LONG), MapKeyType
    data object SFixed32Type : NonDeclType(INT), MapKeyType
    data object SFixed64Type : NonDeclType(LONG), MapKeyType
    data object BoolType : NonDeclType(BOOLEAN), MapKeyType
    data object StringType : NonDeclType(STRING), MapKeyType
    data object BytesType : NonDeclType(BYTE_ARRAY)

    /**
     * Message or Enum Types
     */
    data class DefType(val declaration: String, val ctx: ParserRuleContext) : ProtoType {
        override lateinit var parent: Parent

        override fun resolve(): TypeName {
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

            return decl.typeName
        }

        fun copy(declaration: String): DefType {
            val new = DefType(declaration, ctx)
            new.parent = parent
            return new
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
