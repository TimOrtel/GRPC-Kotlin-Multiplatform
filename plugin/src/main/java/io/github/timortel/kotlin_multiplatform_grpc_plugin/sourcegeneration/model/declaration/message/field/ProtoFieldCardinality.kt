package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.message.field

sealed interface ProtoFieldCardinality {
    sealed interface Singular : ProtoFieldCardinality

    data object Optional : ProtoFieldCardinality, Singular
    data object Implicit : ProtoFieldCardinality, Singular
    data object Repeated : ProtoFieldCardinality
}
