package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.CompilationException
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoNode
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.toFilePositionString

data class ProtoExtensionRanges(val ranges: List<ProtoRange> = emptyList()) : ProtoNode {

    lateinit var message: ProtoMessage

    companion object {
        fun List<ProtoExtensionRanges>.fold(): ProtoExtensionRanges = fold(ProtoExtensionRanges()) { l, r -> l + r }
    }

    operator fun contains(num: Int): Boolean {
        return ranges.any { num in it }
    }

    operator fun plus(other: ProtoExtensionRanges): ProtoExtensionRanges {
        return ProtoExtensionRanges(ranges + other.ranges)
    }

    override fun validate() {
        val sortedRanges = ranges.sortedBy { it.range.first }

        (1 until sortedRanges.size).forEach { index ->
            val currentRange = sortedRanges[index]
            val prevRange = sortedRanges[index - 1]
            if (currentRange.range.first in prevRange) {
                throw CompilationException.ExtensionRangeOverlap(
                    "Extension range at ${currentRange.ctx.toFilePositionString(message.file.path)} overlaps with the range defined at ${
                        prevRange.ctx.toFilePositionString(
                            message.file.path
                        )
                    }",
                    message.file,
                    message.ctx
                )
            }
        }

        ranges
            .filter { it.range.first < 1 || it.range.last > Const.FIELD_NUMBER_MAX_VALUE }
            .forEach { range ->
                throw CompilationException.ExtensionInvalidRange(
                    message = "Extension range is defined out of legal bounds. Minimum value is 1 and maximum value is ${Const.FIELD_NUMBER_MAX_VALUE}",
                    file = message.file,
                    ctx = range.ctx
                )
            }
    }
}
