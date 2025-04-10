package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions.functions

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SourceTarget
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.extensions.MessageWriterExtension
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.ProtoMessageProperty

object HashCodeFunctionExtension : MessageWriterExtension {

    override fun applyToClass(builder: TypeSpec.Builder, message: ProtoMessage, sourceTarget: SourceTarget) {
        val isActual = sourceTarget is SourceTarget.Actual

        builder.addFunction(
            FunSpec.builder(Const.Message.BasicFunctions.HashCodeFunction.NAME)
                .returns(INT)
                .addModifiers(
                    if (isActual) KModifier.ACTUAL else KModifier.EXPECT,
                    KModifier.OVERRIDE
                )
                .apply {
                    if (isActual) {
                        writeHashCodeFunction(this, message)
                    }
                }
                .build()
        )
    }

    private fun writeHashCodeFunction(builder: FunSpec.Builder, message: ProtoMessage) {
        val properties: List<ProtoMessageProperty> = message.fields + message.mapFields + message.oneOfs

        builder.apply {
            if (properties.isEmpty()) {
                addStatement("return %N.hashCode()", Const.Message.Constructor.UnknownFields.name)
                return
            }

            properties.forEachIndexed { index, property ->
                val attrName = property.attributeName

                //Mimic the way IntelliJ generates hashCode
                if (index == 0) {
                    addStatement("var result = %N.hashCode()", attrName)
                } else {
                    addStatement("result = 31 * result + %N.hashCode()", attrName)
                }
            }

            addStatement("result = 31 * result + %N.hashCode()", Const.Message.Constructor.UnknownFields.name)

            addStatement("return result")
        }
    }
}
