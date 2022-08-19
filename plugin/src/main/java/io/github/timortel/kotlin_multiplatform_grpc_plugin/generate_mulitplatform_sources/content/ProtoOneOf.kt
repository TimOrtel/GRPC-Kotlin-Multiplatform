package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content

import com.squareup.kotlinpoet.CodeBlock
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const

/**
 * @property oneOfIndex the index of the oneof in the message.
 */
data class ProtoOneOf(val name: String, val attributes: List<ProtoMessageAttribute>, val oneOfIndex: Int) {
    val capitalizedName: String = name.capitalize()

    fun defaultValue(message: ProtoMessage): CodeBlock = CodeBlock.of("%T", Const.Message.OneOf.notSetClassName(message, this))
}