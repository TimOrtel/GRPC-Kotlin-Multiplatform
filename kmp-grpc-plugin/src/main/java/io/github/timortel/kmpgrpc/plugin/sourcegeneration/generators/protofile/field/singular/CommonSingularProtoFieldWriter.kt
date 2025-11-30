package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.singular

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMessageField

object CommonSingularProtoFieldWriter : SingularProtoFieldWriter() {
    override val attrs: List<KModifier> = emptyList()

    override fun addField(builder: TypeSpec.Builder, field: ProtoMessageField) {
        super.addField(builder, field)

        if (field.hasIsSetProperty && field.isIsSetPropertyPublic) {
            addIsSetProperty(builder, field)
        }
    }

    override fun PropertySpec.Builder.modifyProperty(field: ProtoMessageField) = Unit

    override fun PropertySpec.Builder.modifyIsSetProperty(field: ProtoMessageField) = Unit
}
