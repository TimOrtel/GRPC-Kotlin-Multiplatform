package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.service

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MemberName
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.constants.Const
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.service.ProtoRpc

abstract class IosJvmProtoServiceWriter : ActualProtoServiceWriter() {
    abstract val unaryCallMemberName: MemberName
    abstract val clientStreamingCallMemberName: MemberName
    abstract val serverStreamingCallMemberName: MemberName
    abstract val bidiStreamingCallMemberName: MemberName

    override fun applyToRpcFunction(builder: FunSpec.Builder, rpc: ProtoRpc) {
        val memberName = when (rpc.methodType) {
            ProtoRpc.MethodType.UNARY -> unaryCallMemberName
            ProtoRpc.MethodType.CLIENT_STREAMING -> clientStreamingCallMemberName
            ProtoRpc.MethodType.SERVER_STREAMING -> serverStreamingCallMemberName
            ProtoRpc.MethodType.BIDI_STREAMING -> bidiStreamingCallMemberName
        }

        val requestParamName =
            if (rpc.isSendingStream) Const.Service.RpcCall.PARAM_REQUESTS
            else Const.Service.RpcCall.PARAM_REQUEST

        applyToRpcFunction(builder, rpc, memberName, requestParamName)
    }

    protected abstract fun applyToRpcFunction(builder: FunSpec.Builder, rpc: ProtoRpc, rpcImplementation: MemberName, requestParamName: String)
}
