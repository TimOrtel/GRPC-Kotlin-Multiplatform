package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.enumeration

import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.MemberName.Companion.member
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoOption
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.ProtoEnum
import org.antlr.v4.runtime.ParserRuleContext

data class ProtoEnumField(
    val name: String,
    val number: Int,
    val options: List<ProtoOption>,
    val ctx: ParserRuleContext
) {
    lateinit var enum: ProtoEnum

    val memberName: MemberName get() = enum.className.member(name)
}