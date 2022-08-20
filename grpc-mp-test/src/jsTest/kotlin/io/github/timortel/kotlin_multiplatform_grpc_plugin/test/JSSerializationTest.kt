package io.github.timortel.kotlin_multiplatform_grpc_plugin.test

import io.github.timortel.kotlin_multiplatform_grpc_lib.message.JSImpl
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.KMMessage
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.MessageDeserializer
import io.github.timortel.kotlin_multiplatform_grpc_plugin.test.basic_messages.*
import kotlin.test.Test
import kotlin.test.assertEquals

class JSSerializationTest : SerializationTest() {

    private fun <T : JSImpl, J : KMMessage> serializeImpl(msg: T, deserializer: MessageDeserializer<T>, creator: (T) -> J): J {
        return creator(deserializer.deserializeBinary(msg.serializeBinary()))
    }

    override fun serialize(message: KMLongMessage): KMLongMessage =
        serializeImpl(message.jsImpl, JS_LongMessage.Companion, ::KMLongMessage)

    override fun serialize(message: KMRepeatedLongMessage): KMRepeatedLongMessage =
        serializeImpl(message.jsImpl, JS_RepeatedLongMessage.Companion, ::KMRepeatedLongMessage)

    override fun serialize(message: KMScalarTypes): KMScalarTypes =
        serializeImpl(message.jsImpl, JS_ScalarTypes.Companion, ::KMScalarTypes)

    override fun serialize(message: KMMessageWithSubMessage): KMMessageWithSubMessage =
        serializeImpl(message.jsImpl, JS_MessageWithSubMessage.Companion, ::KMMessageWithSubMessage)

    override fun serialize(message: KMMessageWithEverything): KMMessageWithEverything =
        serializeImpl(message.jsImpl, JS_MessageWithEverything.Companion, ::KMMessageWithEverything)


}