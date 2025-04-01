package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field

import com.squareup.kotlinpoet.ClassName
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.capitalize
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoOption
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.ProtoOneOf
import org.antlr.v4.runtime.ParserRuleContext

data class ProtoOneOfField(
    override val type: ProtoType,
    override val name: String,
    override val number: Int,
    override val options: List<ProtoOption>,
    override val ctx: ParserRuleContext
) : ProtoRegularField() {
    lateinit var parent: ProtoOneOf

    override val file: ProtoFile get() = parent.file

    override val attributeName: String = name

    val sealedClassChildName: ClassName get() = parent.sealedClassName.nestedClass(name.capitalize())

    init {
        type.parent = ProtoType.Parent.OneOfField(this)
    }
}
