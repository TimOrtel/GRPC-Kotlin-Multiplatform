package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field

sealed interface ProtoFieldCardinality {

    data class Singular(val presence: ProtoFieldPresence) : ProtoFieldCardinality

    data object Repeated : ProtoFieldCardinality
}

val ProtoFieldCardinality.isExplicit: Boolean get() = when (this) {
    is ProtoFieldCardinality.Singular -> presence == ProtoFieldPresence.EXPLICIT
    ProtoFieldCardinality.Repeated -> false
}

val ProtoFieldCardinality.isImplicit: Boolean get() = when (this) {
    is ProtoFieldCardinality.Singular -> presence == ProtoFieldPresence.IMPLICIT
    ProtoFieldCardinality.Repeated -> false
}

val ProtoFieldCardinality.isExplicitOrLegacy: Boolean get() = when (this) {
    is ProtoFieldCardinality.Singular -> presence == ProtoFieldPresence.EXPLICIT || presence == ProtoFieldPresence.LEGACY_REQUIRED
    ProtoFieldCardinality.Repeated -> false
}
