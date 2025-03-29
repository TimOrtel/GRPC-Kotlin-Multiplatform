package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.enumeration

import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.MemberName.Companion.member
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.ProtoOption
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.ProtoEnum
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.ProtoField
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.file.ProtoFile
import org.antlr.v4.runtime.ParserRuleContext

data class ProtoEnumField(
    override val name: String,
    override val number: Int,
    override val options: List<ProtoOption>,
    override val ctx: ParserRuleContext
) : ProtoField {
    lateinit var enum: ProtoEnum

    val memberName: MemberName get() = enum.className.member(name)

    override val file: ProtoFile
        get() = enum.file
}
