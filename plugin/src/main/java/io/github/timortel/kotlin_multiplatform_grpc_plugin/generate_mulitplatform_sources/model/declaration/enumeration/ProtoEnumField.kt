package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.enumeration

import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.MemberName.Companion.member
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.ProtoOption
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.ProtoEnum

data class ProtoEnumField(
    val name: String,
    val number: Int,
    val options: List<ProtoOption>
) {
    lateinit var enum: ProtoEnum

    val memberName: MemberName get() = enum.className.member(name)
}