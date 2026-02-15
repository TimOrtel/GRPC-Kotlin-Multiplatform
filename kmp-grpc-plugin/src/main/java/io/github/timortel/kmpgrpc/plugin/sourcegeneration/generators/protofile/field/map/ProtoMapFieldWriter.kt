package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.map

import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.BaseProtoFieldWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMapField

abstract class ProtoMapFieldWriter : BaseProtoFieldWriter {

    fun addMapField(builder: TypeSpec.Builder, field: ProtoMapField) {
        builder.addProperty(
            PropertySpec
                .builder(
                    field.codeName,
                    MAP.parameterizedBy(field.keyType.resolve(), field.valuesType.resolve())
                )
                .addKdoc(field.infoText)
                .apply {
                    modifyMapProperty(this, field)
                    applyDeprecatedOption(field)
                }
                .build()
        )
    }

    protected abstract fun modifyMapProperty(builder: PropertySpec.Builder, field: ProtoMapField)
}
