@file:JsModule("grpc-web")
@file:JsNonModule

package io.github.timortel.kmpgrpc.core.external

import org.khronos.webgl.Uint8Array
import kotlin.js.Promise

external class GrpcWebClientBase(options: ClientOptions) {
    fun <REQ, RESP> unaryCall(
        method: String,
        requestMessage: REQ,
        metadata: dynamic,
        methodDescriptor: dynamic
    ): Promise<RESP>

    fun serverStreaming(
        method: String,
        requestMessage: dynamic,
        metadata: dynamic,
        methodDescriptor: dynamic
    ): dynamic
}

external interface ClientOptions {
    var suppressCorsPreflight: Boolean?
    var withCredentials: Boolean?
    var unaryInterceptors: Array<UnaryInterceptor>?
    var streamInterceptors: Array<StreamInterceptor>?
    var format: String?
    var workerScope: dynamic
    var useFetchDownloadStreams: Boolean?
}

external class MethodDescriptor(
    var name: String,
    var methodType: String,
    var requestType: dynamic,
    var responseType: dynamic,
    var requestSerializeFn: dynamic,
    var responseDeserializeFn: (Uint8Array) -> Any
) : MethodDescriptorInterface {
    override fun getName(): String {
        definedExternally
    }

    override fun getMethodType(): String {
        definedExternally
    }
}

external interface MethodDescriptorInterface {
    fun getName(): String

    fun getMethodType(): String
}

external interface UnaryInterceptor {
    fun <RESP> intercept(
        request: Request,
        invoker: (dynamic) -> Promise<UnaryResponse<RESP>>
    ): Promise<UnaryResponse<RESP>>
}

external interface StreamInterceptor {
    fun intercept(request: Request, invoker: (dynamic) -> ClientReadableStream): ClientReadableStream
}

external interface Request {
    fun getRequestMessage(): dynamic

    fun getMethodDescriptor(): MethodDescriptor

    fun getMetadata(): dynamic

    fun getCallOptions(): CallOptions
}

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

external interface UnaryResponse<RESP> {
    fun getResponseMessage(): RESP

    fun getMetadata(): Metadata

    fun getStatus(): Status

    fun getMethodDescriptor(): MethodDescriptor
}

external class Status {
    var code: Number
    var details: String
    var metadata: Map<String, String>?
}

external interface ClientReadableStream {
    fun on(eventType: String, callback: (dynamic) -> Unit): ClientReadableStream

    fun cancel(): ClientReadableStream
}

external interface Metadata

external class RpcError : Throwable {
    var code: Number
    override var message: String
}
