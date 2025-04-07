package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.singular

import com.squareup.kotlinpoet.*
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMessageField

abstract class SingularProtoFieldWriter {

    protected abstract val attrs: List<KModifier>

    fun addField(builder: TypeSpec.Builder, field: ProtoMessageField) {
        with(builder) {
            addProperty(
                PropertySpec
                    .builder(field.attributeName, field.type.resolve())
                    .addKdoc(field.infoText)
                    .addModifiers(attrs)
                    .apply {
                        modifyProperty(field)
                    }
                    .build()
            )


            if (field.needsIsSetProperty) {
                addProperty(
                    PropertySpec
                        .builder(field.isSetProperty.attributeName, BOOLEAN)
                        .addKdoc(field.infoText)
                        .addModifiers(attrs)
                        .apply { modifyIsSetProperty(field) }
                        .build()
                )
            }
        }
    }

    protected abstract fun PropertySpec.Builder.modifyProperty(field: ProtoMessageField)

    protected abstract fun PropertySpec.Builder.modifyIsSetProperty(field: ProtoMessageField)
}
