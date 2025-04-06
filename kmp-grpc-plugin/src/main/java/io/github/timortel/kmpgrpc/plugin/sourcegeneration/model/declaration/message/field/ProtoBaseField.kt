package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoChildProperty
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoField

sealed class ProtoBaseField : ProtoField, ProtoChildProperty {
    /**
     * The proto number in the message field as defined by the proto source code.
     */
    abstract override val number: Int
    abstract override val name: String

    override val priority: Int
        get() = number

    /**
     * Extra info text for KDOC.
     */
    val infoText: String
        get() = "$name = $number"
}
