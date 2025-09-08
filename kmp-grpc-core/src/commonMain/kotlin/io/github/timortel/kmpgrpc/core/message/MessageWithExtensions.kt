package io.github.timortel.kmpgrpc.core.message

import io.github.timortel.kmpgrpc.core.message.extensions.MessageExtensions

/**
 * Represents a message that supports extensions. This interface extends the base [Message] interface and provides
 * access to extensions associated with the message.
 *
 * @param M The type of the message implementing this interface.
 */
interface MessageWithExtensions<M : Message> : Message {

    /**
     * Holds the collection of values for the extensions associated with this message.
     */
    val messageExtensions: MessageExtensions<M>
}
