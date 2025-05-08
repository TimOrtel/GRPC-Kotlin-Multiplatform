package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SourceTarget

object NativeProtoMessageWriter : NativeJvmProtoMessageWriteBase() {

    override val target: SourceTarget = SourceTarget.Native
}