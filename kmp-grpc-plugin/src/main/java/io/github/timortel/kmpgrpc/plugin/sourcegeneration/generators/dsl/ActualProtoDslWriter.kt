package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.dsl

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.uninitializedMessageException
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.MessageConstructorCallWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMessageField

object ActualProtoDslWriter : ProtoDslWriter(true) {

    override fun modifyBuildFunction(builder: FunSpec.Builder, message: ProtoMessage) {
        builder.apply {
            addCode("val msg = ")

            addCode(
                MessageConstructorCallWriter.getConstructorCallCode(
                    message = message,
                    type = MessageConstructorCallWriter.ConstructorType.BUILD_PARTIAL,
                    getFieldParameter = { field ->
                        if (field.isConstructorParameterNullable(ProtoMessageField.ConstructorParameterType.CREATE_PARTIAL)) {
                            CodeBlock.of("%N", field.codeName)
                        } else {
                            CodeBlock.builder()
                                .add("%N ?: ", field.codeName)
                                .add(field.defaultValue())
                                .build()
                        }
                    },
                    getMapFieldParameter = { field ->
                        CodeBlock.of("%N ?: emptyMap()", field.codeName)
                    },
                    getOneOfFieldParameter = { oneOf ->
                        CodeBlock.of("%N", oneOf.codeName)
                    },
                    getUnknownFieldsParameter = { null },
                    getExtensionParameter = { CodeBlock.of("%N.build()", Const.DSL.MessageExtensions.name) }
                )
            )

            addCode("\n")

            addStatement("if (!msg.isInitialized) throw %T(msg)", uninitializedMessageException)

            addStatement("return msg")
        }
    }
}
