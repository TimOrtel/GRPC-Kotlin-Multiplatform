package io.github.timortel.kotlin_multiplatform_grpc_lib.rpc

import com.google.protobuf.MessageLite
import io.github.timortel.kotlin_multiplatform_grpc_lib.KMChannel
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.KMMessage
import io.github.timortel.kotlin_multiplatform_grpc_lib.message.MessageDeserializer
import io.grpc.CallOptions
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.kotlin.ClientCalls
import io.grpc.protobuf.lite.ProtoLiteUtils

suspend fun <REQ, RES> unaryCallImplementation(
    channel: KMChannel,
    methodDescriptor: MethodDescriptor<REQ, RES>,
    request: REQ,
    defaultRequest: REQ,
    defaultResponse: RES,
    callOptions: CallOptions,
    headers: Metadata
): RES where REQ : KMMessage, REQ : MessageLite, RES : KMMessage, RES : MessageLite {
    return ClientCalls.unaryRpc(
        channel = channel.managedChannel,
        method = MethodDescriptor.newBuilder<REQ, RES>()
            .setType(MethodDescriptor.MethodType.UNARY)
            .setFullMethodName("bla")
            .setRequestMarshaller(ProtoLiteUtils.marshaller(defaultRequest))
            .setResponseMarshaller(ProtoLiteUtils.marshaller(defaultResponse))
            .build(),
        request = request,
        callOptions = callOptions,
        headers = headers
    )
}