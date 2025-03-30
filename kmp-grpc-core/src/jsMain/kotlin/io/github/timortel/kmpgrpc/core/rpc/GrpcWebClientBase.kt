@file:JsModule("grpc-web")

package io.github.timortel.kmpgrpc.core.rpc

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
    suppressCorsPreflight: dynamic,
    withCredentials: dynamic,
    unaryInterceptors: dynamic,
    streamInterceptors: dynamic,
    format: dynamic,
    workerScope: dynamic,
    useFetchDownloadStreams: dynamic
) {
    var format: dynamic
}

external class MethodDescriptor(
    name: String,
    methodType: String,
    requestType: dynamic,
    responseType: dynamic,
    requestSerializeFn: dynamic,
    responseDeserializeFn: dynamic
)