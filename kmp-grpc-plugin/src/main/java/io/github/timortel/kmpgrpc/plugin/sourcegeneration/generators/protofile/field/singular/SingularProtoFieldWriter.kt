package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.singular

import com.squareup.kotlinpoet.*
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoType
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoFieldCardinality
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMessageField

abstract class SingularProtoFieldWriter {

    protected abstract val attrs: List<KModifier>

    fun addField(builder: TypeSpec.Builder, field: ProtoMessageField, cardinality: ProtoFieldCardinality.Singular) {
        with(builder) {
            addProperty(
                PropertySpec
                    .builder(field.name, field.type.resolve())
                    .addModifiers(attrs)
                    .apply {
                        modifyProperty(field)
                    }
                    .build()
            )

            // See https://protobuf.dev/programming-guides/field_presence/#presence-in-proto3-apis
            // The "isSet" method is added for optional fields and message types.
            if (cardinality is ProtoFieldCardinality.Optional || field.type is ProtoType.DefType && field.type.isMessage) {
                addProperty(
                    PropertySpec
                        .builder(field.isSetPropertyName, BOOLEAN)
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
