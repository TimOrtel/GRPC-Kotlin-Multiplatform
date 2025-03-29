package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.message.field

import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.ProtoField
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.message.ProtoMessageProperty

sealed class ProtoBaseField : ProtoMessageProperty, ProtoField {
    /**
     * The proto number in the message field as defined by the proto source code.
     */
    abstract override val number: Int
    abstract override val name: String
}
