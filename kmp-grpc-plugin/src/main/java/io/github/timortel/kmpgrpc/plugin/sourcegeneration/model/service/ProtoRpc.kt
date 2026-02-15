package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.service

import dev.turingcomplete.textcaseconverter.StandardTextCases
import dev.turingcomplete.textcaseconverter.TextCase
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.CodeNameResolver
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoOption
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoOptionsHolder
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoProject
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.SourceCodeNamedNode
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option.OptionTarget
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.capitalize

data class ProtoRpc(
    override val name: String,
    val sendType: ProtoType.DefType,
    val returnType: ProtoType.DefType,
    val isSendingStream: Boolean,
    val isReceivingStream: Boolean,
    override val options: List<ProtoOption>
) : ProtoOptionsHolder, SourceCodeNamedNode {
    lateinit var service: ProtoService

    override val file: ProtoFile get() = service.file

    override val project: ProtoProject get() = file.project

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

    override val kotlinIdiomaticTextCase: TextCase = StandardTextCases.SOFT_CAMEL_CASE

    override val codeNameResolver: CodeNameResolver
        get() = service

    override val priority: Int
        get() = 1

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
