package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.capitalize
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoOption
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoOptionsHolder
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoChildPropertyNameResolver
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.ProtoOneOf
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option.OptionTarget
import org.antlr.v4.runtime.ParserRuleContext

data class ProtoOneOfField(
    override val type: ProtoType,
    override val name: String,
    override val number: Int,
    override val options: List<ProtoOption>,
    override val ctx: ParserRuleContext
) : ProtoRegularField() {

    lateinit var parent: ProtoOneOf

    override val parentOptionsHolder: ProtoOptionsHolder
        get() = parent.message

    override val file: ProtoFile get() = parent.file

    override val desiredAttributeName: String
        get() = name

    override val resolvingParent: ProtoChildPropertyNameResolver
        get() = parent

    val sealedClassChildName: ClassName
        get() = parent.sealedClassName.nestedClass(name.capitalize())

    override val propertyType: TypeName
        get() = sealedClassChildName

    override val isPacked: Boolean = false

    override val optionTarget: OptionTarget get() = OptionTarget.FIELD(type = OptionTarget.FIELD.Type.OneOf)

    init {
        type.parent = ProtoType.Parent.OneOfField(this)
    }
}
