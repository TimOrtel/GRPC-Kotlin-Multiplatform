package io.github.timortel.kmpgrpc.core

import io.github.timortel.kmpgrpc.core.message.KMMessage

interface CallInterceptor {

    fun onStart(methodDescriptor: KMMethodDescriptor, metadata: KMMetadata): KMMetadata = metadata

    fun <T : KMMessage> onSendMessage(methodDescriptor: KMMethodDescriptor, message: T): T = message

    fun onReceiveHeaders(methodDescriptor: KMMethodDescriptor, metadata: KMMetadata): KMMetadata = metadata

    fun <T : KMMessage> onReceiveMessage(methodDescriptor: KMMethodDescriptor, message: T): T = message

    fun onClose(
        methodDescriptor: KMMethodDescriptor,
        status: KMStatus,
        metadata: KMMetadata
    ): Pair<KMStatus, KMMetadata> = status to metadata
}
