package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kotlin_multiplatform_grpc_lib.message.KMMessage
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.MessageDeserializer
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.basic_messages.*

class JSSerializationTest : SerializationTest() {

    private fun <T : KMMessage> serializeImpl(msg: T, deserializer: MessageDeserializer<T, dynamic>): T {
        val buffer = msg.serialize()
        return deserializer.deserialize(buffer)
    }

    override fun serialize(message: KMLongMessage): KMLongMessage =
        serializeImpl(message, KMLongMessage.Companion)

    override fun serialize(message: KMRepeatedLongMessage): KMRepeatedLongMessage =
        serializeImpl(message, KMRepeatedLongMessage.Companion)

    override fun serialize(message: KMScalarTypes): KMScalarTypes = serializeImpl(message, KMScalarTypes.Companion)

    override fun serialize(message: KMMessageWithSubMessage): KMMessageWithSubMessage =
        serializeImpl(message, KMMessageWithSubMessage.Companion)

    override fun serialize(message: KMMessageWithEverything): KMMessageWithEverything =
        serializeImpl(message, KMMessageWithEverything.Companion)

    override fun serialize(message: KMOneOfMessage): KMOneOfMessage = serializeImpl(message, KMOneOfMessage.Companion)

    override fun serialize(message: KMComplexRepeatedMessage): KMComplexRepeatedMessage = serializeImpl(message, KMComplexRepeatedMessage.Companion)
}