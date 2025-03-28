package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.enumeration

import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.MemberName.Companion.member
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoOption
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.ProtoEnum
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.ProtoField
import org.antlr.v4.runtime.ParserRuleContext

data class ProtoEnumField(
    override val name: String,
    override val number: Int,
    val options: List<ProtoOption>,
    override val ctx: ParserRuleContext
) : ProtoField {
    lateinit var enum: ProtoEnum

    val memberName: MemberName get() = enum.className.member(name)
}
