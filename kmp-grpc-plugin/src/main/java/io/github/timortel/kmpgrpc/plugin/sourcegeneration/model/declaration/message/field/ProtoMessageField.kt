package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoOption
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoChildProperty
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoChildPropertyNameResolver
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.ProtoMessageProperty
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.capitalize
import org.antlr.v4.runtime.ParserRuleContext

data class ProtoMessageField(
    override val type: ProtoType,
    override val name: String,
    override val number: Int,
    override val options: List<ProtoOption>,
    val cardinality: ProtoFieldCardinality,
    override val ctx: ParserRuleContext
) : ProtoRegularField(), ProtoMessageProperty {

    lateinit var parent: ProtoMessage

    override val message: ProtoMessage
        get() = parent

    override val file: ProtoFile get() = parent.file

    override val desiredAttributeName: String = when (cardinality) {
        ProtoFieldCardinality.Implicit, ProtoFieldCardinality.Optional -> name
        ProtoFieldCardinality.Repeated -> "${name}List"
    }

    override val propertyType: TypeName
        get() = when (cardinality) {
            is ProtoFieldCardinality.Singular -> {
                type.resolve()
            }
            is ProtoFieldCardinality.Repeated -> {
                LIST.parameterizedBy(type.resolve())
            }
        }

    // See https://protobuf.dev/programming-guides/field_presence/#presence-in-proto3-apis
    // The "isSet" method is added for optional fields and message types.
    val needsIsSetProperty: Boolean
        get() = cardinality is ProtoFieldCardinality.Optional || (type is ProtoType.DefType && type.isMessage)

    val isSetProperty: ExtraProperty
        get() = ExtraProperty(
            desiredAttributeName = "is${name.capitalize()}Set",
            resolvingParent = message,
            priority = number,
            propertyType = BOOLEAN
        )

    val childProperties: List<ProtoChildProperty>
        get() = if (needsIsSetProperty) listOf(isSetProperty) else emptyList()

    init {
        type.parent = ProtoType.Parent.MessageField(this)
    }

    fun defaultValue(): CodeBlock {
        return when (cardinality) {
            ProtoFieldCardinality.Implicit, ProtoFieldCardinality.Optional -> type.defaultValue()
            ProtoFieldCardinality.Repeated -> CodeBlock.of("emptyList()")
        }
    }

    data class ExtraProperty(
        override val desiredAttributeName: String,
        override val resolvingParent: ProtoChildPropertyNameResolver,
        override val priority: Int,
        override val propertyType: TypeName
    ) : ProtoChildProperty {
        override val name: String
            get() = desiredAttributeName
    }
}
