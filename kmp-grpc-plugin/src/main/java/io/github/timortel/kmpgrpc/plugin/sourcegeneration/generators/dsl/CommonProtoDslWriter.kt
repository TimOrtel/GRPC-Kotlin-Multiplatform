package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.dsl

import com.squareup.kotlinpoet.FunSpec
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage

object CommonProtoDslWriter : ProtoDslWriter(false) {

    override fun modifyBuildFunction(
        builder: FunSpec.Builder,
        message: ProtoMessage
    ) = Unit
}