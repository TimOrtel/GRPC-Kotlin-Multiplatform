package io.github.timortel.kmpgrpc.core

import io.github.timortel.kmpgrpc.core.message.KMMessage

/**
 * Base interface for intercepting calls.
 */
interface CallInterceptor {

    /**
     * Intercept on start.
     * @param metadata the metadata that is intended to be sent to the server.
     * @return the metadata that should be sent to the server.
     */
    fun onStart(methodDescriptor: KMMethodDescriptor, metadata: KMMetadata): KMMetadata = metadata

    /**
     * Intercept sending a message.
     * @param message the message that is intended to be sent to the server.
     * @return the message that should be sent to the server.
     */
    fun <T : KMMessage> onSendMessage(methodDescriptor: KMMethodDescriptor, message: T): T = message

    /**
     * Intercept receiving the initial headers from the server. Always called before [onReceiveMessage].
     *
     * Note: due to a limitation of the gRPC implementation on JavaScript, this function is not called for server streaming calls.
     *
     * @param metadata the metadata received from the server. Potentially modified by previous [CallInterceptor]s.
     * @return the metadata that should be forwarded.
     */
    fun onReceiveHeaders(methodDescriptor: KMMethodDescriptor, metadata: KMMetadata): KMMetadata = metadata

    /**
     * Intercept receiving a message from the server.
     * @param message the message received from the server. Potentially modified by previous [CallInterceptor]s.
     * @return the message that should be forwarded.
     */
    fun <T : KMMessage> onReceiveMessage(methodDescriptor: KMMethodDescriptor, message: T): T = message

    /**
     * Intercept closing the call.
     * @param status the status received for this call. Potentially modified by previous [CallInterceptor]s.
     * @param metadata the trailing headers received by the server. Potentially modified by previous [CallInterceptor]s.
     * @return the status and metadata that should be forwarded.
     */
    fun onClose(
        methodDescriptor: KMMethodDescriptor,
        status: KMStatus,
        metadata: KMMetadata
    ): Pair<KMStatus, KMMetadata> = status to metadata
}
