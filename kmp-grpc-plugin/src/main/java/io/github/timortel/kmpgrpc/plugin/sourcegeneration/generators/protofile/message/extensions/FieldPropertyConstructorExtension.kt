package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SourceTarget
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoFieldCardinality

object FieldPropertyConstructorExtension : MessageWriterExtension {

    override fun applyToConstructor(builder: FunSpec.Builder, message: ProtoMessage, sourceTarget: SourceTarget) {
        val isActual = sourceTarget is SourceTarget.Actual

        //one of attributes do not get a parameter, as they get the one of parameter
        message.fields.forEach { field ->
            when (field.cardinality) {
                is ProtoFieldCardinality.Singular -> {
                    val type = if (field.type.isMessage) field.type.resolve().copy(nullable = true)
                    else field.type.resolve()

                    builder.addParameter(
                        ParameterSpec
                            .builder(field.attributeName, type)
                            .apply {
                                if (!isActual) defaultValue(field.type.defaultValue())
                            }
                            .build()
                    )
                }

                ProtoFieldCardinality.Repeated -> {
                    builder.addParameter(
                        ParameterSpec
                            .builder(field.attributeName, LIST.parameterizedBy(field.type.resolve()))
                            .apply {
                                if (!isActual) defaultValue("emptyList()")
                            }
                            .build()
                    )
                }
            }
        }

        message.mapFields.forEach { mapField ->
            builder.addParameter(
                ParameterSpec
                    .builder(
                        mapField.attributeName,
                        MAP.parameterizedBy(
                            mapField.keyType.resolve(),
                            mapField.valuesType.resolve(),
                        )
                    )
                    .apply {
                        if (!isActual) defaultValue("emptyMap()")
                    }
                    .build()
            )
        }

        message.oneOfs.forEach { oneOf ->
            builder.addParameter(
                ParameterSpec
                    .builder(
                        oneOf.attributeName,
                        oneOf.sealedClassName
                    )
                    .apply {
                        if (!isActual) defaultValue("%T", oneOf.sealedClassNameNotSet)
                    }
                    .build()
            )
        }
    }
}
