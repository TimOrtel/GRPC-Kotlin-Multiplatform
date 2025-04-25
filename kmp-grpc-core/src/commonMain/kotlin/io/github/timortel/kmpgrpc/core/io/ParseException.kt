package io.github.timortel.kmpgrpc.core.io

class ParseException(override val message: String? = null) : Exception() {
    companion object {
        fun negativeSize(size: Int) = ParseException("Length delimited size must not be negative but was $size")
    }
}
