package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.message.field

import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoOption
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.message.ProtoMessageProperty

sealed class ProtoBaseField : ProtoMessageProperty {
    /**
     * The proto number in the message field as defined by the proto source code.
     */
    abstract val number: Int
    abstract val options: List<ProtoOption>
}