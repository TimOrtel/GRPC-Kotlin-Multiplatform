package io.github.timortel.kmpgrpc.wkt.ext

import com.google.protobuf.Any
import com.google.protobuf.any
import io.github.timortel.kmpgrpc.core.message.Message
import io.github.timortel.kmpgrpc.core.message.MessageCompanion
import io.github.timortel.kmpgrpc.core.message.MessageDeserializer
import io.github.timortel.kmpgrpc.core.message.extensions.ExtensionRegistry

private const val DEFAULT_TYPE_URL_PREFIX = "type.googleapis.com"

/**
 * @return a new [Any] message with the given [message] as content.
 */
public fun <T : Message> Any.Companion.wrap(message: T, typeUrlPrefix: String = DEFAULT_TYPE_URL_PREFIX): Any = any {
    type_url = getTypeUrl(message.fullName, typeUrlPrefix)
    value = message.serialize()
}

/**
 * Constructs a message of type [T] by using the data held by this [Any] object.
 * @param deserializer typically the companion object of [T].
 * @param extensionRegistry the extension registry. By default, an empty registry is used.
 * @return a message of type [T]
 */
public fun <T : Message, DES : MessageDeserializer<T>> Any.unwrap(deserializer: DES, extensionRegistry: ExtensionRegistry<T> = ExtensionRegistry.empty()): T {
    return deserializer.deserialize(value, extensionRegistry)
}

/**
 * Checks if the [Any.type_url] matches [T] using [typeUrlPrefix].
 *
 * @param message the companion object of [T]
 * @return if the type held by this object matches [T]
 */
public fun <T : Message> Any.isType(message: MessageCompanion<T>, typeUrlPrefix: String): Boolean {
    return type_url == getTypeUrl(message.fullName, typeUrlPrefix)
}

/**
 * Checks if the [Any.type_url] matches [T] by checking against the [MessageCompanion.fullName].
 *
 * @param message the companion object of [T]
 * @return if the type held by this object matches [T]
 */
public fun <T : Message> Any.isType(message: MessageCompanion<T>): Boolean {
    return type_url.substringAfterLast('/') == message.fullName
}

/**
 * Checks if the [Any.type_url] matches the type of [message] using [typeUrlPrefix].
 *
 * @return if the type held by this object matches the type of [message]
 */
public fun Any.isSameTypeAs(message: Message, typeUrlPrefix: String): Boolean {
    return type_url == getTypeUrl(message.fullName, typeUrlPrefix)
}

/**
 * Checks if the [Any.type_url] matches the type of [message] by checking against the [Message.fullName].
 *
 * @return if the type held by this object matches the type of [message]
 */
public fun Any.isSameTypeAs(message: Message): Boolean {
    return type_url.substringAfterLast('/') == message.fullName
}

private fun getTypeUrl(fullName: String, typeUrlPrefix: String): String =
    typeUrlPrefix.removeSuffix("/") + "/" + fullName
