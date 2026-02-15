package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.DeclarationResolver
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoNode
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoOption
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoProject
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoChildProperty
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.CodeNameResolver
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoOneOfField

data class ProtoOneOf(
    override val name: String,
    val fields: List<ProtoOneOfField>,
    val options: List<ProtoOption>
) : ProtoMessageProperty, ProtoNode, CodeNameResolver, OneOfSealedClassNameHolder {

    companion object {
        private const val UNSET_CLASS_NAME = "NotSet"
    }

    override lateinit var message: ProtoMessage

    override val file: ProtoFile get() = message.file

    val sealedClassName: ClassName get() = message.className.nestedClass(sealedClassRawName)

    val sealedClassNameNotSet: ClassName get() = sealedClassName.nestedClass(UNSET_CLASS_NAME)

    override val project: ProtoProject
        get () = file.project

    override val propertyType: TypeName
        get() = sealedClassName

    override val consideredNodes: List<ProtoChildProperty>
        get() = fields

    override val reservedNames: Set<String>
        get() = Const.Message.OneOf.reservedClassNames

    override val priority: Int
        get() = fields.minOf { it.number }

    override val declarationResolver: DeclarationResolver
        get() = message

    init {
        fields.forEach { it.parent = this }
    }

    override fun validate() {
        fields.forEach { it.validate() }
    }
}
