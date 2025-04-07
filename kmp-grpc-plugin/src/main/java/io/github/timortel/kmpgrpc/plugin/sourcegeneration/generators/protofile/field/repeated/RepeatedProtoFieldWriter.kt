package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.repeated

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMessageField

abstract class RepeatedProtoFieldWriter {

    protected abstract val attrs: List<KModifier>

    fun addField(builder: TypeSpec.Builder, field: ProtoMessageField) {
        val listProperty = PropertySpec
            .builder(
                field.attributeName,
                LIST.parameterizedBy(field.type.resolve())
            )
            .addKdoc(field.infoText)
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
