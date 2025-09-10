package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.DeclarationResolver
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.Options
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoExtensionDefinition
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoOption
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoChildProperty
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoChildPropertyNameResolver
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.ProtoMessageProperty
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType.MessageDefaultValue
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

    lateinit var parent: Parent

    override val message: ProtoMessage
        get() = when (val p = parent) {
            is Parent.Message -> p.message
            is Parent.ExtensionDefinition -> p.ext.messageType.resolveDeclaration() as ProtoMessage
        }

    override val file: ProtoFile get() = when (val p = parent) {
        is Parent.ExtensionDefinition -> p.ext.file
        is Parent.Message -> p.message.file
    }

    override val declarationResolver: DeclarationResolver
        get() = when (val p = parent) {
            is Parent.ExtensionDefinition -> p.ext.parent
            is Parent.Message -> p.message
        }

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
            resolvingParent = resolvingParent,
            priority = number,
            propertyType = BOOLEAN
        )

    val childProperties: List<ProtoChildProperty>
        get() = if (needsIsSetProperty) listOf(isSetProperty) else emptyList()

    override val supportedOptions: List<Options.Option<*>>
        get() = super.supportedOptions + listOf(Options.packed)

    /**
     * True iff [cardinality] is [ProtoFieldCardinality.Repeated], the [type] is packable and the proto option packed is not set to false
     */
    override val isPacked: Boolean
        get() = cardinality == ProtoFieldCardinality.Repeated && type.isPackable && Options.packed.get(this)

    init {
        type.parent = ProtoType.Parent.MessageField(this)
    }

    fun defaultValue(messageDefaultValue: MessageDefaultValue = MessageDefaultValue.NULL): CodeBlock {
        return when (cardinality) {
            ProtoFieldCardinality.Implicit, ProtoFieldCardinality.Optional -> type.defaultValue(messageDefaultValue)
            ProtoFieldCardinality.Repeated -> CodeBlock.of("emptyList()")
        }
    }

    override fun isSupportedOptionValid(option: ProtoOption): Boolean {
        return when (option.name) {
            Options.packed.name -> {
                // packed option is only valid on repeated fields that have a packable type
                cardinality == ProtoFieldCardinality.Repeated && type.isPackable
            }
            else -> super.isSupportedOptionValid(option)
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

    sealed interface Parent {
        data class Message(val message: ProtoMessage) : Parent
        data class ExtensionDefinition(val ext: ProtoExtensionDefinition) : Parent
    }
}
