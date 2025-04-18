package io.github.timortel.kmpgrpc.core

import io.github.timortel.kmpgrpc.core.message.Message

/**
 * Base interface for intercepting calls.
 */
interface CallInterceptor {

    /**
     * Intercept on start.
     * @param metadata the metadata that is intended to be sent to the server.
     * @return the metadata that should be sent to the server.
     */
    fun onStart(methodDescriptor: MethodDescriptor, metadata: Metadata): Metadata = metadata

    /**
     * Intercept sending a message.
     * @param message the message that is intended to be sent to the server.
     * @return the message that should be sent to the server.
     */
    fun <T : Message> onSendMessage(methodDescriptor: MethodDescriptor, message: T): T = message

    /**
     * Intercept receiving the initial headers from the server. Always called before [onReceiveMessage].
     *
     * Note: due to a limitation of the gRPC implementation on JavaScript, this function is not called for server streaming calls.
     *
     * @param metadata the metadata received from the server. Potentially modified by previous [CallInterceptor]s.
     * @return the metadata that should be forwarded.
     */
    fun onReceiveHeaders(methodDescriptor: MethodDescriptor, metadata: Metadata): Metadata = metadata

    /**
     * Intercept receiving a message from the server.
     * @param message the message received from the server. Potentially modified by previous [CallInterceptor]s.
     * @return the message that should be forwarded.
     */
    fun <T : Message> onReceiveMessage(methodDescriptor: MethodDescriptor, message: T): T = message

    /**
     * Intercept closing the call.
     * @param status the status received for this call. Potentially modified by previous [CallInterceptor]s.
     * @param metadata the trailing headers received by the server. Potentially modified by previous [CallInterceptor]s.
     * @return the status and metadata that should be forwarded.
     */
    fun onClose(
        methodDescriptor: MethodDescriptor,
        status: Status,
        metadata: Metadata
    ): Pair<Status, Metadata> = status to metadata
}
