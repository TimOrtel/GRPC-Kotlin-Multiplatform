package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.message

data class ProtoReservation(val nums: List<Int> = emptyList(), val ranges: List<IntRange> = emptyList()) {

    companion object {
        fun List<ProtoReservation>.fold(): ProtoReservation = fold(ProtoReservation()) { l, r -> l + r }
    }

    operator fun contains(num: Int): Boolean {
        return num in nums || ranges.any { num in it }
    }

    operator fun plus(other: ProtoReservation): ProtoReservation {
        return ProtoReservation(nums + other.nums, ranges + other.ranges)
    }
}
