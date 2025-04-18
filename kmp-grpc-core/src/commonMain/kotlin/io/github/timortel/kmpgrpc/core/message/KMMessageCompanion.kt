package io.github.timortel.kmpgrpc.core.message

/**
 * Base interface for all companion objects of [Message]
 * @param T the message
 */
interface KMMessageCompanion<T : Message> {
    /**
     * The name of this proto: <proto-package>.<name>
     */
    val fullName: String
}
