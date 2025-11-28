package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field

sealed interface ProtoFieldCardinality {
    sealed interface Singular : ProtoFieldCardinality

    data object Explicit : ProtoFieldCardinality, Singular
    data object Implicit : ProtoFieldCardinality, Singular
    data object Repeated : ProtoFieldCardinality
}
