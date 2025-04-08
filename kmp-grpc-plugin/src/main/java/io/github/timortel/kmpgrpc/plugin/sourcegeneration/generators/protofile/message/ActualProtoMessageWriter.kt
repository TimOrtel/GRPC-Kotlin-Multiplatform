package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.kmMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.enumeration.ActualProtoEnumerationWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.enumeration.ProtoEnumerationWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.ActualProtoFieldWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.ProtoFieldWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage

abstract class ActualProtoMessageWriter : ProtoMessageWriter(true) {

    override val protoFieldWriter: ProtoFieldWriter = ActualProtoFieldWriter
    override val protoEnumerationWriter: ProtoEnumerationWriter = ActualProtoEnumerationWriter

    override fun applyToClass(builder: TypeSpec.Builder, message: ProtoMessage) {
        builder.apply {
            addSuperinterface(kmMessage)

            addProperty(
                Const.Message.fullNameProperty.toPropertySpecBuilder(KModifier.ACTUAL, KModifier.OVERRIDE)
                    .initializer("%T.%N", message.className, Const.Message.Companion.fullNameProperty.name)
                    .build()
            )
        }
    }


    override fun applyToCompanionObject(builder: TypeSpec.Builder, message: ProtoMessage) {
        builder.apply {
            addModifiers(KModifier.ACTUAL)

            builder.addProperty(
                Const.Message.Companion.fullNameProperty.toPropertySpecBuilder(KModifier.ACTUAL, KModifier.OVERRIDE)
                    .initializer("%S", message.fullName)
                    .build()
            )
        }
    }
}