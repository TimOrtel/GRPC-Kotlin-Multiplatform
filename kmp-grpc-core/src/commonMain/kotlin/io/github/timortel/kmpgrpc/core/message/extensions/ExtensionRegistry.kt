package io.github.timortel.kmpgrpc.core.message.extensions

import io.github.timortel.kmpgrpc.core.message.Message

/**
 * A registry for managing protocol buffer extensions associated with a specific message type.
 *
 * @param M The type of the message associated with the extensions in the registry.
 */
class ExtensionRegistry<M : Message> private constructor(
    internal val extensionMap: Map<Int, Extension<M, *>>
) {
    companion object {

        /**
         * Creates and returns an empty [ExtensionRegistry] for a given message type.
         *
         * @return An empty [ExtensionRegistry] instance designed for managing extensions of the specified message type.
         */
        fun <M : Message> empty(): ExtensionRegistry<M> = ExtensionRegistry(emptyMap())

        /**
         * Creates an [ExtensionRegistry] instance containing the provided extensions.
         *
         * @param extensions The extensions to include in the registry. Each extension is associated
         * with a specific field number and message type.
         * @return An [ExtensionRegistry] containing the given extensions.
         */
        fun <M : Message> of(vararg extensions: Extension<M, *>): ExtensionRegistry<M> {
            return of(extensions.toList())
        }

        /**
         * Creates an [ExtensionRegistry] instance containing the provided extensions.
         *
         * @param extensions The extensions to include in the registry. Each extension is associated
         * with a specific field number and message type.
         * @return An [ExtensionRegistry] containing the given extensions.
         */
        fun <M : Message> of(extensions: List<Extension<M, *>>): ExtensionRegistry<M> {
            return ExtensionRegistry(extensions.associateBy { it.fieldNumber })
        }
    }

    internal fun getExtensionForFieldNumber(fieldNumber: Int): Extension<M, *>? {
        return extensionMap[fieldNumber]
    }
}
