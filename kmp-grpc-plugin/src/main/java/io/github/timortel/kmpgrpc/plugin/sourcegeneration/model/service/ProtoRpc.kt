package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.service

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoOption
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoOptionsHolder
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option.OptionTarget
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.capitalize

data class ProtoRpc(
    val name: String,
    val sendType: ProtoType.DefType,
    val returnType: ProtoType.DefType,
    val isSendingStream: Boolean,
    val isReceivingStream: Boolean,
    override val options: List<ProtoOption>
) : ProtoOptionsHolder {
    lateinit var service: ProtoService

    override val file: ProtoFile get() = service.file

    val jvmMethodDescriptorName: String = "methodDescriptor${name.capitalize()}"

    override val optionTarget: OptionTarget = OptionTarget.METHOD

    override val parentOptionsHolder: ProtoOptionsHolder
        get() = service

    val methodType: MethodType = when {
        isSendingStream && isReceivingStream -> MethodType.BIDI_STREAMING
        isSendingStream -> MethodType.CLIENT_STREAMING
        isReceivingStream -> MethodType.SERVER_STREAMING
        else -> MethodType.UNARY
    }

    init {
        sendType.parent = ProtoType.Parent.Rpc(this)
        returnType.parent = ProtoType.Parent.Rpc(this)
    }

    override fun validate() {
        super.validate()

        sendType.validate()
        returnType.validate()
    }

    enum class MethodType(val jvmMethodType: String) {
        UNARY("UNARY"),
        CLIENT_STREAMING("CLIENT_STREAMING"),
        SERVER_STREAMING("SERVER_STREAMING"),
        BIDI_STREAMING("BIDI_STREAMING")
    }
}
