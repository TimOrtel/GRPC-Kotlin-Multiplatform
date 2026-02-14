package io.github.timortel.kmpgrpc.core

import io.github.timortel.kmpgrpc.core.message.Message

/**
 * Thrown when a Protocol Buffers message is missing one or more required fields.
 *
 * In `proto2`, fields marked as `required` must be populated before a message
 * can be fully initialized or serialized. This exception typically occurs during
 * a DSL `build()` operation or when parsing a message that violates these
 * presence constraints.
 *
 * @property msg The incomplete [Message] instance that triggered this exception.
 * Note that accessing fields on this instance is safe, but it is considered
 * semantically invalid according to the schema.
 */
class UninitializedMessageException(
    val msg: Message,
) : RuntimeException(
    "Message ${msg::class.simpleName} is missing required fields."
)
