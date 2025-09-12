package io.github.timortel.kmpgrpc.core.message.extensions

import io.github.timortel.kmpgrpc.core.message.Message
import io.github.timortel.kmpgrpc.shared.internal.InternalKmpGrpcApi

/**
 * Builder class for constructing instances of [MessageExtensions].
 *
 * @param M The type of the message associated with the extensions being built.
 */
class MessageExtensionsBuilder<M : Message> @InternalKmpGrpcApi constructor() {

    private val scalarMap = mutableMapOf<Extension.ScalarValueExtension<M, Any>, Any>()
    private val repeatedMap = mutableMapOf<Extension.RepeatedValueExtension<M, Any>, List<Any>>()

    /**
     * Sets the specified value for the given extensions. If a value already exists, it is overwritten.
     *
     * @param extension The scalar value extension which defines the field and message type.
     * @param value The scalar value to associate with the provided extension.
     */
    operator fun <T : Any> set(extension: Extension.ScalarValueExtension<M, T>, value: T) {
        @Suppress("UNCHECKED_CAST")
        scalarMap[extension as Extension.ScalarValueExtension<M, Any>] = value as Any
    }

    /**
     * Adds the given value to the list of values associated with the specified repeated extension.
     * If the extension already exists in the map, the value is appended to the existing list.
     * If the extension does not exist in the map, a new list is created with the given value as its sole entry.
     *
     * @param extension The repeated value extension that defines the field and message type.
     * @param value The value to append or associate with the provided extension.
     */
    fun <T : Any> setOrAppend(extension: Extension.RepeatedValueExtension<M, T>, value: T) {
        setOrAppend(extension, listOf(value))
    }

    /**
     * Adds the given values to the list of values associated with the specified repeated extension.
     * If the extension already exists in the map, the values are appended to the existing list.
     *
     * @param extension The repeated value extension that defines the field and message type.
     * @param values The list of values to append or associate with the provided extension.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> setOrAppend(extension: Extension.RepeatedValueExtension<M, T>, values: List<T>) {
        val ext = extension as Extension.RepeatedValueExtension<M, Any>

        val existingValue = repeatedMap[ext]
        if (existingValue != null) {
            repeatedMap[ext] = existingValue + values.toList() as List<Any>
        } else {
            repeatedMap[ext] = values.toList() as List<Any>
        }
    }

    /**
     * Adds the provided list of values to the map associated with the specified repeated value extension.
     * Any existing values for the given extension will be replaced with the provided list.
     *
     * @param extension The repeated value extension that defines the field and message type.
     * @param values The list of values to associate with the provided extension.
     */
    operator fun <T : Any> set(extension: Extension.RepeatedValueExtension<M, T>, values: List<T>) {
        @Suppress("UNCHECKED_CAST")
        repeatedMap[extension as Extension.RepeatedValueExtension<M, Any>] = (values.toList() as List<Any>)
    }

    /**
     * Adds all extensions from the provided [MessageExtensions] to the current builder.
     * Any existing entries in the scalar or repeated maps will be updated with the corresponding entries
     * from the provided extensions.
     *
     * @param extensions The [MessageExtensions] instance containing the scalar and repeated extensions
     * to be added to the current builder.
     */
    fun setAll(extensions: MessageExtensions<M>) {
        scalarMap += extensions.scalarMap
        repeatedMap += extensions.repeatedMap
    }

    /**
     * Clears all extensions stored in the builder, effectively resetting the builder to an empty state.
     */
    fun clear() {
        scalarMap.clear()
        repeatedMap.clear()
    }

    @InternalKmpGrpcApi
    fun build(): MessageExtensions<M> {
        return MessageExtensions(scalarMap, repeatedMap)
    }
}

/**
 * Builds a [MessageExtensions] instance by applying the provided block to a [MessageExtensionsBuilder].
 *
 * @param block A lambda that applies configuration to the [MessageExtensionsBuilder] instance.
 * @return A [MessageExtensions] instance containing the configured extensions.
 */
fun <M : Message> buildExtensions(block: MessageExtensionsBuilder<M>.() -> Unit): MessageExtensions<M> {
    val builder = MessageExtensionsBuilder<M>()
    block(builder)
    return builder.build()
}
