package io.github.timortel.kmpgrpc.core.message

import io.github.timortel.kmpgrpc.core.message.extensions.ExtensionRegistry

/**
 * Base interface for all companion objects of [MessageWithExtensions]
 * @param T the message
 */
interface MessageWithExtensionsCompanion<T : Message> : MessageCompanion<T> {

    /**
     * The default extension registry for the message. Has all extensions registered that are known for the message type.
     */
    val defaultExtensionRegistry: ExtensionRegistry<T>
}
