package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.singular

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.BaseProtoFieldWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMessageField

abstract class SingularProtoFieldWriter : BaseProtoFieldWriter {

    protected abstract val attrs: List<KModifier>

    open fun addField(builder: TypeSpec.Builder, field: ProtoMessageField) {
        with(builder) {
            addProperty(
                PropertySpec
                    .builder(field.attributeName, field.type.resolve())
                    .addKdoc(field.infoText)
                    .addModifiers(attrs)
                    .apply {
                        modifyProperty(field)
                        applyDeprecatedOption(field)
                    }
                    .build()
            )
        }
    }

    protected fun addIsSetProperty(builder: TypeSpec.Builder, field: ProtoMessageField) {
        builder.addProperty(
            PropertySpec
                .builder(field.isSetProperty.attributeName, BOOLEAN)
                .addKdoc(field.infoText)
                .apply {
                    if (!field.isIsSetPropertyPublic) addModifiers(KModifier.PRIVATE)
                    else addModifiers(attrs)

                    modifyIsSetProperty(field)
                }
                .build()
        )
    }

    protected abstract fun PropertySpec.Builder.modifyProperty(field: ProtoMessageField)

    protected abstract fun PropertySpec.Builder.modifyIsSetProperty(field: ProtoMessageField)
}
