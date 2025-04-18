package io.github.timortel.kmpgrpc.wkt.ext

import com.google.protobuf.Any
import com.google.protobuf.any
import io.github.timortel.kmpgrpc.core.message.Message
import io.github.timortel.kmpgrpc.core.message.KMMessageCompanion
import io.github.timortel.kmpgrpc.core.message.MessageDeserializer

private const val DEFAULT_TYPE_URL_PREFIX = "type.googleapis.com"

/**
 * @return a new [Any] message with the given [message] as content.
 */
fun <T : Message> Any.Companion.wrap(message: T, typeUrlPrefix: String = DEFAULT_TYPE_URL_PREFIX): Any = any {
    type_url = getTypeUrl(message.fullName, typeUrlPrefix)
    value = message.serialize()
}

/**
 * Constructs a message of type [T] by using the data held by this [Any] object.
 * @param deserializer typically the companion object of [T].
 * @return a message of type [T]
 */
inline fun <T : Message, DES : MessageDeserializer<T>> Any.unwrap(deserializer: DES): T {
    return deserializer.deserialize(value)
}

/**
 * Checks if the [Any.type_url] matches [T] using [typeUrlPrefix].
 *
 * @param message the companion object of [T]
 * @return if the type held by this object matches [T]
 */
fun <T : Message> Any.isType(message: KMMessageCompanion<T>, typeUrlPrefix: String): Boolean {
    return type_url == getTypeUrl(message.fullName, typeUrlPrefix)
}

/**
 * Checks if the [Any.type_url] matches [T] by checking against the [KMMessageCompanion.fullName].
 *
 * @param message the companion object of [T]
 * @return if the type held by this object matches [T]
 */
fun <T : Message> Any.isType(message: KMMessageCompanion<T>): Boolean {
    return type_url.substringAfterLast('/') == message.fullName
}

/**
 * Checks if the [Any.type_url] matches the type of [message] using [typeUrlPrefix].
 *
 * @return if the type held by this object matches the type of [message]
 */
fun Any.isSameTypeAs(message: Message, typeUrlPrefix: String): Boolean {
    return type_url == getTypeUrl(message.fullName, typeUrlPrefix)
}

/**
 * Checks if the [Any.type_url] matches the type of [message] by checking against the [Message.fullName].
 *
 * @return if the type held by this object matches the type of [message]
 */
fun Any.isSameTypeAs(message: Message): Boolean {
    return type_url.substringAfterLast('/') == message.fullName
}

private fun getTypeUrl(fullName: String, typeUrlPrefix: String): String =
    typeUrlPrefix.removeSuffix("/") + "/" + fullName
