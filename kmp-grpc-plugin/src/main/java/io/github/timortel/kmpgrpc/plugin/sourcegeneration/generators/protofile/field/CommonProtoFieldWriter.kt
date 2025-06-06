package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.map.CommonProtoMapFieldWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.map.ProtoMapFieldWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.repeated.CommonRepeatedProtoFieldWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.repeated.RepeatedProtoFieldWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.singular.CommonSingularProtoFieldWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.field.singular.SingularProtoFieldWriter

object CommonProtoFieldWriter : ProtoFieldWriter() {
    override val singularProtoFieldWriter: SingularProtoFieldWriter = CommonSingularProtoFieldWriter
    override val repeatedProtoFieldWriter: RepeatedProtoFieldWriter = CommonRepeatedProtoFieldWriter
    override val mapProtoFieldWriter: ProtoMapFieldWriter = CommonProtoMapFieldWriter
}
