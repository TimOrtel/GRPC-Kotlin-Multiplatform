package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.map.ActualProtoMapFieldWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.map.ProtoMapFieldWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.repeated.ActualRepeatedProtoFieldWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.repeated.RepeatedProtoFieldWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.singular.ActualSingularProtoFieldWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.singular.SingularProtoFieldWriter

object ActualProtoFieldWriter : ProtoFieldWriter() {
    override val singularProtoFieldWriter: SingularProtoFieldWriter = ActualSingularProtoFieldWriter
    override val repeatedProtoFieldWriter: RepeatedProtoFieldWriter = ActualRepeatedProtoFieldWriter
    override val mapProtoFieldWriter: ProtoMapFieldWriter = ActualProtoMapFieldWriter
}
