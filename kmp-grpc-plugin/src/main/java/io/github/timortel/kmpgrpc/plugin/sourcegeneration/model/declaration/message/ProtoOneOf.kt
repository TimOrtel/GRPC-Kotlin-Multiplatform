package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message

import com.squareup.kotlinpoet.ClassName
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoNode
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.capitalize
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoOption
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoOneOfField

data class ProtoOneOf(
    override val name: String,
    val fields: List<ProtoOneOfField>,
    val options: List<ProtoOption>
) : ProtoMessageProperty, ProtoNode {
    companion object {
        private const val UNKNOWN_CLASS_NAME = "Unknown"
        private const val UNSET_CLASS_NAME = "NotSet"
    }

    lateinit var message: ProtoMessage

    val file: ProtoFile get() = message.file

    val sealedClassName: ClassName get() = message.className.nestedClass(name.capitalize())

    val sealedClassNameNotSet: ClassName get() = sealedClassName.nestedClass(UNSET_CLASS_NAME)
    val sealedClassNameUnknown: ClassName get() = sealedClassName.nestedClass(UNKNOWN_CLASS_NAME)

    override val attributeName: String = name

    init {
        fields.forEach { it.parent = this }
    }

    override fun validate() {
        fields.forEach { it.validate() }
    }
}
