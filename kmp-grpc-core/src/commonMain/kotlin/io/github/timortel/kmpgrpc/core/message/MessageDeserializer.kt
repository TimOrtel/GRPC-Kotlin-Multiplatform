package io.github.timortel.kmpgrpc.core.message

import io.github.timortel.kmpgrpc.core.io.CodedInputStream
import io.github.timortel.kmpgrpc.core.message.extensions.ExtensionRegistry

/**
 * Interface for deserializing messages from various input sources into instances of type [T].
 * Type [T] represents a class that implements the [Message] interface and represents a gRPC message.
 *
 * Used to handle deserialization of serialized message data back into their structured [Message] representations.
 *
 * @param T The type of the message to be deserialized. Must extend [Message].
 */
expect interface MessageDeserializer<T : Message> {

    /**
     * Deserializes a message from the provided byte array and constructs an instance of type [T].
     *
     * @param data The serialized message data in the form of a byte array.
     * @param extensionRegistry The extension registry to be used.
     * @return An instance of [T] constructed from the deserialized data.
     */
    open fun deserialize(`data`: ByteArray, extensionRegistry: ExtensionRegistry<T>): T

    /**
     * Deserializes a message from the provided byte array and constructs an instance of type [T].
     * The appropriate default extension registry is picked for deserialization.
     *
     * @param data The serialized message data in the form of a byte array.
     * @return An instance of [T] constructed from the deserialized data.
     */
    fun deserialize(`data`: ByteArray): T

    /**
     * Deserializes data from a provided [CodedInputStream] and constructs an instance of type [T].
     *
     * @param stream The [CodedInputStream] containing the serialized message data.
     * @param extensionRegistry The extension registry to be used.
     * @return An instance of [T] constructed from the deserialized data.
     */
    fun deserialize(stream: CodedInputStream, extensionRegistry: ExtensionRegistry<T>): T

    /**
     * Deserializes data from a provided [CodedInputStream] and constructs an instance of type [T].
     * The appropriate default extension registry is picked for deserialization.

     *
     * @param stream The [CodedInputStream] containing the serialized message data.
     * @return An instance of [T] constructed from the deserialized data.
     */
    fun deserialize(stream: CodedInputStream): T
}
