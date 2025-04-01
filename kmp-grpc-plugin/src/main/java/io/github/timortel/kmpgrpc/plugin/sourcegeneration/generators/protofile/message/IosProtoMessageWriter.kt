package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SourceTarget

object IosProtoMessageWriter : IosJvmProtoMessageWriteBase() {

    override val target: SourceTarget = SourceTarget.Ios
}