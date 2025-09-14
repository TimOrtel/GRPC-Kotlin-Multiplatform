package io.github.timortel.kmpgrpc.core.message.extensions

import io.github.timortel.kmpgrpc.core.message.Message

/**
 * Represents the extensions set for an instance of a message. Use [get] to retrieve the value for an extension.
 */
class MessageExtensions<M : Message>(
    internal val scalarMap: Map<Extension.ScalarValueExtension<M, Any>, Any> = emptyMap(),
    internal val repeatedMap: Map<Extension.RepeatedValueExtension<M, Any>, List<Any>> = emptyMap(),
) {

    val requiredSize: Int =
        scalarMap.entries.sumOf { (ext, value) -> ext.computeRequiredSize(value) } +
                repeatedMap.entries.sumOf { (ext, value) -> ext.computeRequiredSize(value) }

    /**
     * Retrieves the scalar value associated with the given scalar extension.
     *
     * @param extension The scalar extension for which the value is to be retrieved.
     * @return The value associated with the specified scalar extension, or null if no value is found.
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(extension: Extension.ScalarValueExtension<M, T>): T? {
        return scalarMap[extension as Extension.ScalarValueExtension<M, Any>] as T?
    }

    /**
     * Retrieves the list of values associated with the given repeated extension.
     *
     * @param extension The repeated value extension for which the values are to be retrieved.
     * @return A list of values associated with the specified repeated extension, or an empty list if no values are found.
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(extension: Extension.RepeatedValueExtension<M, T>): List<T> {
        return repeatedMap[extension as Extension.RepeatedValueExtension<M, Any>].orEmpty() as List<T>
    }

    override fun toString(): String {
        return buildString {
            append("{")

            val entries = scalarMap.entries + repeatedMap.entries
            entries.forEachIndexed { i, (key, value) ->
                append(key)
                append("=")
                append(value)

                if (i != entries.indices.last) {
                    append(", ")
                }
            }
            append("}")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as MessageExtensions<*>

        if (!mapsEqual(scalarMap, other.scalarMap)) return false
        if (!mapsEqual(repeatedMap, other.repeatedMap)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = scalarMap.hashCode()
        result = 31 * result + repeatedMap.hashCode()
        return result
    }

    private fun mapsEqual(map1: Map<*, Any>, map2: Map<*, Any>): Boolean {
        if (map1.size != map2.size) return false

        for ((key, value1) in map1) {
            val value2 = map2[key] ?: return false

            val equal = when (value1) {
                is ByteArray if value2 is ByteArray -> value1.contentEquals(value2)
                is List<*> if value2 is List<*> -> listsEqual(value1, value2)
                else -> value1 == value2
            }

            if (!equal) return false
        }
        return true
    }

    private fun listsEqual(l1: List<*>, l2: List<*>): Boolean {
        if (l1.size != l2.size) return false

        return l1.zip(l2).all { (a, b) ->
            if (a is ByteArray && b is ByteArray) {
                a.contentEquals(b)
            } else a == b
        }
    }
}
