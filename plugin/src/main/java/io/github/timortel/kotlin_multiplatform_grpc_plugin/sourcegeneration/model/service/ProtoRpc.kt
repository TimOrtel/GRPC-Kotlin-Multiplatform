package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.service

import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.capitalize
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.decapitalize
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoOption
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoType

data class ProtoRpc(
    val name: String,
    val sendType: ProtoType.DefType,
    val returnType: ProtoType.DefType,
    val isSendingStream: Boolean,
    val isReceivingStream: Boolean,
    val options: List<ProtoOption>
) {
    lateinit var service: ProtoService

    val file: ProtoFile get() = service.file

    val jvmMethodDescriptorName: String = "methodDescriptor${name.capitalize()}"
    val jsMethodDescriptorName: String = "${name.decapitalize()}MethodDescriptor"

    init {
        sendType.parent = ProtoType.Parent.Rpc(this)
        returnType.parent = ProtoType.Parent.Rpc(this)
    }
}
