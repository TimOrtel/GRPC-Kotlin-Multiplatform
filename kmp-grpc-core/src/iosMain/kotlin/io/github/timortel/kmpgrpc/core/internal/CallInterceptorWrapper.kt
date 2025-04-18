package io.github.timortel.kmpgrpc.core.internal

import cocoapods.GRPCClient.*
import io.github.timortel.kmpgrpc.core.*
import io.github.timortel.kmpgrpc.core.message.Message
import io.github.timortel.kmpgrpc.core.message.MessageDeserializer
import io.github.timortel.kmpgrpc.core.rpc.asGrpcStatus
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSLocalizedDescriptionKey
import platform.darwin.dispatch_queue_t

private const val ERROR_DOMAIN = "io.grpc"

internal class CallInterceptorWrapper<REQ : Message, RESP : Message>(
    private val interceptor: CallInterceptor,
    private val methodDescriptor: MethodDescriptor,
    private val requestDeserializer: MessageDeserializer<REQ>,
    private val responseDeserializer: MessageDeserializer<RESP>,
    val interceptorManager: GRPCInterceptorManager,
    dispatchQueue: dispatch_queue_t,
) : GRPCInterceptor(interceptorManager, dispatchQueue) {

    override fun startWithRequestOptions(requestOptions: GRPCRequestOptions, callOptions: GRPCCallOptions) {
        val newMetadata = interceptor.onStart(methodDescriptor, callOptions.initialMetadata.extractMetadata())

        super.startWithRequestOptions(requestOptions, callOptions.edit { setInitialMetadata(newMetadata.entries.toMap()) })
    }

    override fun writeData(data: Any) {
        val newData = if (data is NSData) {
            interceptor
                .onSendMessage(methodDescriptor, requestDeserializer.deserialize(data))
                .serializeNative()
        } else data

        super.writeData(newData)
    }

    override fun didReceiveInitialMetadata(initialMetadata: Map<Any?, *>?) {
        val newMetadata = interceptor.onReceiveHeaders(methodDescriptor, initialMetadata.extractMetadata())

        super.didReceiveInitialMetadata(newMetadata.entries.toMap())
    }

    override fun didReceiveData(data: Any) {
        val newData = if (data is NSData) {
            interceptor
                .onReceiveMessage(methodDescriptor, responseDeserializer.deserialize(data))
                .serializeNative()
        } else data

        super.didReceiveData(newData)
    }

    override fun didCloseWithTrailingMetadata(trailingMetadata: Map<Any?, *>?, error: NSError?) {
        val (newStatus, newTrailingMetadata) = interceptor.onClose(
            methodDescriptor = methodDescriptor,
            status = error?.asGrpcStatus ?: Status(Code.OK, ""),
            metadata = trailingMetadata.extractMetadata()
        )

        super.didCloseWithTrailingMetadata(
            trailingMetadata = newTrailingMetadata.entries.toMap(),
            error = if (newStatus.code != Code.OK) {
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

    private fun Map<Any?, *>?.extractMetadata(): Metadata {
        return Metadata.of(orEmpty().map { (key, value) -> key.toString() to value.toString() }.toMap())
    }

    private fun GRPCCallOptions.edit(edit: GRPCMutableCallOptions.() -> Unit): GRPCCallOptions {
        return (mutableCopy() as GRPCMutableCallOptions).apply(edit)
    }
}
