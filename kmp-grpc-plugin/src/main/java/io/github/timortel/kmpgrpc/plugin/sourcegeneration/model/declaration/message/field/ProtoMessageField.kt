package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.MemberName.Companion.member
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.CompilationException
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.DeclarationResolver
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoExtensionDefinition
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoLanguageVersion
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoOption
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoOptionsHolder
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoChildProperty
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoChildPropertyNameResolver
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.ProtoMessageProperty
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option.OptionTarget
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option.Options
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType.MessageDefaultValue
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.capitalize
import org.antlr.v4.runtime.ParserRuleContext

class ProtoMessageField(
    override val type: ProtoType,
    override val name: String,
    override val number: Int,
    override val options: List<ProtoOption>,
    private val fieldCardinality: FieldCardinality,
    override val ctx: ParserRuleContext
) : ProtoRegularField(), ProtoMessageProperty {

    lateinit var parent: Parent

    override val parentOptionsHolder: ProtoOptionsHolder?
        get() = when (val p = parent) {
            is Parent.ExtensionDefinition -> when (val p2 = p.ext.parent) {
                is ProtoExtensionDefinition.Parent.File -> p2.file
                is ProtoExtensionDefinition.Parent.Message -> p2.message
            }

            is Parent.Message -> p.message
        }

    override val message: ProtoMessage
        get() = when (val p = parent) {
            is Parent.Message -> p.message
            is Parent.ExtensionDefinition -> p.ext.messageType.resolveDeclaration() as ProtoMessage
        }

    override val file: ProtoFile
        get() = when (val p = parent) {
            is Parent.ExtensionDefinition -> p.ext.file
            is Parent.Message -> p.message.file
        }

    override val declarationResolver: DeclarationResolver
        get() = when (val p = parent) {
            is Parent.ExtensionDefinition -> p.ext.parent
            is Parent.Message -> p.message
        }

    val cardinality: ProtoFieldCardinality
        get() = when (file.languageVersion) {
            ProtoLanguageVersion.PROTO3, ProtoLanguageVersion.PROTO2 -> when (fieldCardinality) {
                FieldCardinality.SINGULAR -> ProtoFieldCardinality.Singular(ProtoFieldPresence.IMPLICIT)
                FieldCardinality.SINGULAR_OPTIONAL -> ProtoFieldCardinality.Singular(ProtoFieldPresence.EXPLICIT)
                FieldCardinality.SINGULAR_REQUIRED -> ProtoFieldCardinality.Singular(ProtoFieldPresence.LEGACY_REQUIRED)
                FieldCardinality.REPEATED -> ProtoFieldCardinality.Repeated
            }

            ProtoLanguageVersion.EDITION2023, ProtoLanguageVersion.EDITION2024 -> when (fieldCardinality) {
                FieldCardinality.SINGULAR -> ProtoFieldCardinality.Singular(
                    presence = Options.Feature.fieldPresence.get(this)
                )

                FieldCardinality.REPEATED -> ProtoFieldCardinality.Repeated
                FieldCardinality.SINGULAR_OPTIONAL, FieldCardinality.SINGULAR_REQUIRED -> throw IllegalArgumentException("field cardinality $fieldCardinality is illegal for edition versions.")
            }
        }

    override val desiredAttributeName: String
        get() = when (cardinality) {
            is ProtoFieldCardinality.Singular -> name
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

    /**
     * If cardinality is either explicit or legacy, or if the type is a message and it is not repeated
     */
    val needsIsSetProperty: Boolean
        get() = cardinality.isExplicit || (type is ProtoType.DefType && type.isMessage && cardinality != ProtoFieldCardinality.Repeated)

    val isSetProperty: ExtraProperty
        get() = ExtraProperty(
            desiredAttributeName = "is${name.capitalize()}Set",
            resolvingParent = resolvingParent,
            priority = number,
            propertyType = BOOLEAN
        )

    val childProperties: List<ProtoChildProperty>
        get() = if (needsIsSetProperty) listOf(isSetProperty) else emptyList()

    /**
     * True iff [cardinality] is [ProtoFieldCardinality.Repeated], the [type] is packable and the proto option packed is not set to false
     */
    override val isPacked: Boolean
        get() = cardinality == ProtoFieldCardinality.Repeated && type.isPackable && when (file.languageVersion) {
            ProtoLanguageVersion.PROTO3, ProtoLanguageVersion.PROTO2 -> Options.Basic.packed.get(this)
            ProtoLanguageVersion.EDITION2023, ProtoLanguageVersion.EDITION2024 ->
                when (Options.Feature.repeatedFieldEncoding.get(this)) {
                    ProtoRepeatedFieldEncoding.PACKED -> true
                    ProtoRepeatedFieldEncoding.EXPANDED -> false
                }
        }

    val memberName: MemberName
        get() {
            val parentClassName = when (val p = parent) {
                is Parent.ExtensionDefinition -> p.ext.parent.className
                is Parent.Message -> p.message.className
            }

            return parentClassName.member(name)
        }

    override val optionTarget: OptionTarget
        get() = OptionTarget.FIELD(
            type = OptionTarget.FIELD.Type.Regular(
                isRepeated = when (cardinality) {
                    ProtoFieldCardinality.Repeated -> true
                    is ProtoFieldCardinality.Singular -> false
                },
                isPackable = type.isPackable
            )
        )

    init {
        type.parent = ProtoType.Parent.MessageField(this)
    }

    fun defaultValue(messageDefaultValue: MessageDefaultValue = MessageDefaultValue.NULL): CodeBlock {
        return when (cardinality) {
            is ProtoFieldCardinality.Singular -> type.defaultValue(messageDefaultValue)
            ProtoFieldCardinality.Repeated -> CodeBlock.of("emptyList()")
        }
    }

    override fun validate() {
        super.validate()

        if (number < 1 || number > Const.FIELD_NUMBER_MAX_VALUE) {
            throw CompilationException.IllegalFieldNumber(
                message = "Field $name declared illegal field number ${number}. Must be >= 1 and <= ${Const.FIELD_NUMBER_MAX_VALUE}",
                file = file,
                ctx = ctx
            )
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

    /**
     * Used as a constructor parameter only.
     */
    enum class FieldCardinality {
        SINGULAR,
        SINGULAR_OPTIONAL,
        SINGULAR_REQUIRED,
        REPEATED
    }
}
