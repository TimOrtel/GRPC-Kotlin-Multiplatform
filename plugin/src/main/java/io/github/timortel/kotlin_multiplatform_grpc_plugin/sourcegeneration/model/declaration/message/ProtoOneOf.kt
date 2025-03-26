package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.message

import com.squareup.kotlinpoet.ClassName
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.util.capitalize
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoOption
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.message.field.ProtoOneOfField

data class ProtoOneOf(
    override val name: String,
    val fields: List<ProtoOneOfField>,
    val options: List<ProtoOption>
) : ProtoMessageProperty {
    companion object {
        private const val UNKNOWN_CLASS_NAME = "Unknown"
        private const val UNSET_CLASS_NAME = "NotSet"
    }

    lateinit var message: ProtoMessage

    val file: ProtoFile get() = message.file

    val sealedClassName: ClassName get() = message.className.nestedClass(name.capitalize())

    val sealedClassNameNotSet: ClassName get() = sealedClassName.nestedClass(UNSET_CLASS_NAME)
    val sealedClassNameUnknown: ClassName get() = sealedClassName.nestedClass(UNKNOWN_CLASS_NAME)

    override val fieldName: String = name

    init {
        fields.forEach { it.parent = this }
    }
}
