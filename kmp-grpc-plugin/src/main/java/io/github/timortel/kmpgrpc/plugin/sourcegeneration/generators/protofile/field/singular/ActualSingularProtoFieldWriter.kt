package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.singular

import com.squareup.kotlinpoet.*
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMessageField

object ActualSingularProtoFieldWriter : SingularProtoFieldWriter() {

    override val attrs: List<KModifier> = listOf(KModifier.ACTUAL)

    override fun PropertySpec.Builder.modifyProperty(field: ProtoMessageField) {
        if (field.needsIsSetProperty) {
            initializer(
                CodeBlock
                    .builder()
                    .apply {
                        add("%N ?: ", field.attributeName)
                        add(field.defaultValue(messageDefaultValue = ProtoType.MessageDefaultValue.EMPTY))
                    }
                    .build()
            )
        } else {
            initializer(field.attributeName)
        }
    }

    override fun PropertySpec.Builder.modifyIsSetProperty(field: ProtoMessageField) {
        initializer("%N != null", field.attributeName)
    }
}
