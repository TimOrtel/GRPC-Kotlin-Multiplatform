package io.github.timortel.kmpgrpc.core.message

/**
 * Representation of an unknown field parsed when reading a [KMMessage].
 */
sealed interface UnknownField {
    val number: Int

    data class Varint(override val number: Int, val value: Long) : UnknownField
    data class Fixed32(override val number: Int, val value: UInt) : UnknownField
    data class Fixed64(override val number: Int, val value: ULong) : UnknownField

    data class LengthDelimited(override val number: Int, val value: ByteArray) : UnknownField {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as LengthDelimited

            if (number != other.number) return false
            if (!value.contentEquals(other.value)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = number
            result = 31 * result + value.contentHashCode()
            return result
        }
    }

    data class Group(override val number: Int, val values: List<UnknownField>) : UnknownField
}
