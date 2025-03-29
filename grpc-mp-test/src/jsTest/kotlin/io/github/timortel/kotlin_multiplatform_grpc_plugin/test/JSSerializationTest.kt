package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kotlin_multiplatform_grpc_lib.message.KMMessage
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.MessageDeserializer
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.basic_messages.*

class JSSerializationTest : SerializationTest() {

    private fun <T : KMMessage> serializeImpl(msg: T, deserializer: MessageDeserializer<T, dynamic>): T {
        val buffer = msg.serialize()
        return deserializer.deserialize(buffer)
    }

    override fun serialize(message: LongMessage): LongMessage =
        serializeImpl(message, LongMessage.Companion)

    override fun serialize(message: RepeatedLongMessage): RepeatedLongMessage =
        serializeImpl(message, RepeatedLongMessage.Companion)

    override fun serialize(message: ScalarTypes): ScalarTypes = serializeImpl(message, ScalarTypes.Companion)

    override fun serialize(message: MessageWithSubMessage): MessageWithSubMessage =
        serializeImpl(message, MessageWithSubMessage.Companion)

    override fun serialize(message: MessageWithEverything): MessageWithEverything =
        serializeImpl(message, MessageWithEverything.Companion)

    override fun serialize(message: OneOfMessage): OneOfMessage = serializeImpl(message, OneOfMessage.Companion)

    override fun serialize(message: ComplexRepeatedMessage): ComplexRepeatedMessage = serializeImpl(message, ComplexRepeatedMessage.Companion)
}