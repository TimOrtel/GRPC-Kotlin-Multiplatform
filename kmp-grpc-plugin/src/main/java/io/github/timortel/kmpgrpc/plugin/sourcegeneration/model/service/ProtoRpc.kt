package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.service

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.Options
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.capitalize
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.decapitalize
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoOption
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoOptionsHolder
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType

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
    val jsMethodDescriptorName: String = "${name.decapitalize()}MethodDescriptor"

    override val supportedOptions: List<Options.Option<*>> = emptyList()

    init {
        sendType.parent = ProtoType.Parent.Rpc(this)
        returnType.parent = ProtoType.Parent.Rpc(this)
    }
}
