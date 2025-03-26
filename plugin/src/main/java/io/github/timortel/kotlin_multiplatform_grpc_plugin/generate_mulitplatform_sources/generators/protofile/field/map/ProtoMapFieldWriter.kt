package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.field.map

import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.field.ProtoMapField

abstract class ProtoMapFieldWriter {

    fun addMapField(builder: TypeSpec.Builder, field: ProtoMapField) {
        builder.addProperty(
            PropertySpec
                .builder(
                    Const.Message.Attribute.Map.propertyName(field),
                    MAP.parameterizedBy(field.keyType.resolve(), field.valuesType.resolve())
                )
                .apply {
                    modifyMapProperty(this, field)
                }
                .build()
        )
    }

    protected abstract fun modifyMapProperty(builder: PropertySpec.Builder, field: ProtoMapField)
}
