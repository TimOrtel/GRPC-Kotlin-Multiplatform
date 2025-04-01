package io.github.timortel.kmpgrpc.core.message

/**
 * Base interface for all companion objects of [KMMessage]
 * @param T the message
 */
interface KMMessageCompanion<T : KMMessage> {
    /**
     * The name of this proto: <proto-package>.<name>
     */
    val fullName: String
}
