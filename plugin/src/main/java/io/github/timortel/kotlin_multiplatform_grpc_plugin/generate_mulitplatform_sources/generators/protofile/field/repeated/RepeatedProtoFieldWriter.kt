package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.field.repeated

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.field.ProtoMessageField

abstract class RepeatedProtoFieldWriter {

    protected abstract val attrs: List<KModifier>

    fun addField(builder: TypeSpec.Builder, field: ProtoMessageField) {
        val listProperty = PropertySpec
            .builder(
                Const.Message.Attribute.Repeated.listPropertyName(field),
                LIST.parameterizedBy(field.type.resolve())
            )
            .addModifiers(attrs)
            .apply { modifyListProperty(this, field) }
            .build()

        builder.addProperty(listProperty)
    }

    protected abstract fun modifyListProperty(
        builder: PropertySpec.Builder,
        field: ProtoMessageField
    )

    protected abstract fun modifyCountProperty(
        builder: PropertySpec.Builder,
        field: ProtoMessageField
    )
}
