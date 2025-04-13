package io.github.timortel.kmpgrpc.core.internal

import cocoapods.GRPCClient.*
import io.github.timortel.kmpgrpc.core.*
import io.github.timortel.kmpgrpc.core.message.KMMessage
import io.github.timortel.kmpgrpc.core.message.MessageDeserializer
import io.github.timortel.kmpgrpc.core.rpc.asGrpcStatus
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSLocalizedDescriptionKey

class CallInterceptorWrapper<REQ : KMMessage, RESP : KMMessage>(
    private val interceptor: CallInterceptor,
    private val methodDescriptor: KMMethodDescriptor,
    private val requestDeserializer: MessageDeserializer<REQ>,
    private val responseDeserializer: MessageDeserializer<RESP>,
    private val interceptorManager: GRPCInterceptorManager,
) : GRPCInterceptor(interceptorManager, null) {

    companion object {
        private const val ERROR_DOMAIN = "io.grpc"
    }

    override fun startWithRequestOptions(requestOptions: GRPCRequestOptions, callOptions: GRPCCallOptions) {
        val newMetadata = interceptor.onStart(methodDescriptor, callOptions.initialMetadata.extractMetadata())

        interceptorManager.startWithRequestOptions(
            requestOptions = requestOptions,
            callOptions = callOptions.edit { setInitialMetadata(newMetadata.metadataMap.toMap()) }
        )
    }

    override fun writeData(data: Any) {
        val newData = if (data is NSData) {
            interceptor
                .onSendMessage(methodDescriptor, requestDeserializer.deserialize(data))
                .serializeNative()
        } else data

        interceptorManager.writeData(newData)
    }

    override fun didReceiveInitialMetadata(initialMetadata: Map<Any?, *>?) {
        val newMetadata = interceptor.onReceiveHeaders(methodDescriptor, initialMetadata.extractMetadata())

        interceptorManager.didReceiveInitialMetadata(newMetadata.metadataMap.toMap())
    }

    override fun didReceiveData(data: Any) {
        val newData = if (data is NSData) {
            interceptor
                .onReceiveMessage(methodDescriptor, responseDeserializer.deserialize(data))
                .serializeNative()
        } else data

        interceptorManager.didReceiveData(newData)
    }

    override fun didCloseWithTrailingMetadata(trailingMetadata: Map<Any?, *>?, error: NSError?) {
        val (newStatus, newTrailingMetadata) = interceptor.onClose(
            methodDescriptor = methodDescriptor,
            status = error?.asGrpcStatus ?: KMStatus(KMCode.OK, ""),
            metadata = trailingMetadata.extractMetadata()
        )

        interceptorManager.didCloseWithTrailingMetadata(
            trailingMetadata = newTrailingMetadata.metadataMap.toMap(),
            error = if (newStatus.code != KMCode.OK) {
                if (error != null) {
                    NSError.errorWithDomain(
                        domain = error.domain,
                        code = newStatus.code.value.toLong(),
                        userInfo = error.userInfo + Pair(NSLocalizedDescriptionKey, newStatus.statusMessage)
                    )
                } else {
                    NSError.errorWithDomain(
                        domain = ERROR_DOMAIN,
                        code = newStatus.code.value.toLong(),
                        userInfo = mapOf(Pair(NSLocalizedDescriptionKey, newStatus.statusMessage))
                    )
                }
            } else {
                null
            }
        )
    }

    private fun Map<Any?, *>?.extractMetadata(): KMMetadata {
        return KMMetadata(orEmpty().map { (key, value) -> key.toString() to value.toString() }.toMap())
    }

    private fun GRPCCallOptions.edit(edit: GRPCMutableCallOptions.() -> Unit): GRPCCallOptions {
        return (mutableCopy() as GRPCMutableCallOptions).apply(edit)
    }
}
