package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.message

data class ProtoReservation(
    val nums: List<Int> = emptyList(),
    val ranges: List<IntRange> = emptyList(),
    val names: List<String> = emptyList()
) {

    companion object {
        fun List<ProtoReservation>.fold(): ProtoReservation = fold(ProtoReservation()) { l, r -> l + r }
    }

    operator fun contains(num: Int): Boolean {
        return num in nums || ranges.any { num in it }
    }

    operator fun contains(name: String): Boolean {
        return name in names
    }

    operator fun plus(other: ProtoReservation): ProtoReservation {
        return ProtoReservation(nums + other.nums, ranges + other.ranges, names + other.names)
    }
}
