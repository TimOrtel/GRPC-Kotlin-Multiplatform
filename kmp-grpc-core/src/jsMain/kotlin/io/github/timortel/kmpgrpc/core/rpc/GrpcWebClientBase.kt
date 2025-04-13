@file:JsModule("grpc-web")

package io.github.timortel.kmpgrpc.core.rpc

import org.khronos.webgl.Uint8Array
import kotlin.js.Promise

external class GrpcWebClientBase(options: ClientOptions) {
    fun rpcCall(
        method: String,
        requestMessage: dynamic,
        metadata: dynamic,
        methodDescriptor: dynamic,
        callback: dynamic
    )

    fun serverStreaming(
        method: String,
        requestMessage: dynamic,
        metadata: dynamic,
        methodDescriptor: dynamic
    ): dynamic
}

external class ClientOptions(
    val suppressCorsPreflight: Boolean?,
    val withCredentials: Boolean?,
    val unaryInterceptors: Array<UnaryInterceptor>,
    val streamInterceptors: Array<StreamInterceptor>,
    val format: String?,
    val workerScope: dynamic,
    val useFetchDownloadStreams: Boolean?
)

external class MethodDescriptor(
    name: String,
    methodType: String,
    requestType: dynamic,
    responseType: dynamic,
    requestSerializeFn: dynamic,
    responseDeserializeFn: (Uint8Array) -> Any
) {
    fun getName(): String
    fun getMethodType(): String
}

external interface UnaryInterceptor {
    fun intercept(request: Request, invoker: (dynamic) -> Promise<UnaryResponse>)
}

external interface StreamInterceptor {
    fun intercept(request: Request, invoker: (dynamic) -> ClientReadableStream): ClientReadableStream
}

external interface Request {
    fun getRequestMessage(): dynamic

    fun getMethodDescriptor(): MethodDescriptor

    fun getMetadata(): Map<String, String>

    fun getCallOptions(): CallOptions
}

external class RequestInternal(
    requestMessage: dynamic,
    methodDescriptor: MethodDescriptor,
    metadata: Map<String, String>,
    callOptions: CallOptions
)

external object MethodType {
    val UNARY: String
    val SERVER_STREAMING: String
    val BIDI_STREAMING: String
}

external class CallOptions {
    fun getKeys(): Array<String>

    fun get(name: String): dynamic

    fun setOption(name: String, value: dynamic)
}

open external class UnaryResponse {
    fun getResponseMessage(): dynamic

    fun getMetadata(): Map<String, String>

    fun getStatus(): Status

    fun getMethodDescriptor(): MethodDescriptor
}

external class UnaryResponseInternal(
    responseMessage: dynamic,
    methodDescriptor: MethodDescriptor,
    metadata: Map<String, String>,
    status: Status
) : UnaryResponse

external class Status(
    val code: Number,
    val details: String,
    val metadata: Map<String, String>?
)

external interface ClientReadableStream {
    fun on(eventType: String, callback: (UnaryResponse) -> Unit): ClientReadableStream

    fun cancel(): ClientReadableStream
}
