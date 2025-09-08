package io.github.timortel.kmpgrpc.core.message.extensions

import io.github.timortel.kmpgrpc.core.message.Message

/**
 * Represents the extensions set for an instance of a message. Use [get] to retrieve the value for an extension.
 */
class MessageExtensions<M : Message>(
    internal val scalarMap: Map<Extension.ScalarValueExtension<M, Any>, Any>,
    internal val repeatedMap: Map<Extension.RepeatedValueExtension<M, Any>, List<Any>>,
) {

    /**
     * Retrieves the scalar value associated with the given scalar extension.
     *
     * @param extension The scalar extension for which the value is to be retrieved.
     * @return The value associated with the specified scalar extension, or null if no value is found.
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(extension: Extension.ScalarValueExtension<M, T>): T? {
        return scalarMap[extension as Extension.ScalarValueExtension<M, Any>] as T?
    }

    /**
     * Retrieves the list of values associated with the given repeated extension.
     *
     * @param extension The repeated value extension for which the values are to be retrieved.
     * @return A list of values associated with the specified repeated extension, or an empty list if no values are found.
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(extension: Extension.RepeatedValueExtension<M, T>): List<T> {
        return repeatedMap[extension as Extension.RepeatedValueExtension<M, Any>].orEmpty() as List<T>
    }
}
