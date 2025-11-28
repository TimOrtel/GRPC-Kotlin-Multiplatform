package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field

import com.squareup.kotlinpoet.PropertySpec
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.DefaultAnnotations
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option.Options
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoBaseField

interface BaseProtoFieldWriter {

    fun PropertySpec.Builder.applyDeprecatedOption(field: ProtoBaseField) {
        if (Options.Basic.deprecated.get(field)) {
            addAnnotation(DefaultAnnotations.Deprecated)
        }
    }
}
