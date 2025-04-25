package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message

import com.squareup.kotlinpoet.TypeName
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SourceTarget

object JvmProtoMessageWriter : IosJvmProtoMessageWriteBase() {

    override val additionalSuperinterfaces: List<TypeName> = listOf()

    override val target: SourceTarget = SourceTarget.Jvm
}
