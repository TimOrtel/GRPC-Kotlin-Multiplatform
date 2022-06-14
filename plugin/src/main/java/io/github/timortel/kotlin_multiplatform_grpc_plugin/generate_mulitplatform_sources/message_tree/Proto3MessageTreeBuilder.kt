package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.message_tree

import io.github.timortel.kotlin_multiplatform_grpc_plugin.anltr.Proto3BaseVisitor
import io.github.timortel.kotlin_multiplatform_grpc_plugin.anltr.Proto3Parser

class Proto3MessageTreeBuilder : Proto3BaseVisitor<List<Node>>() {

    override fun visitFile(ctx: Proto3Parser.FileContext): List<Node> {
        return ctx.message().map { visitMessage(it, null) }.flatten() +
                ctx.proto_enum().map { protoEnum -> EnumNode(protoEnum.enumName.text, null) }
    }

    private fun visitMessage(ctx: Proto3Parser.MessageContext, parentMessageNode: MessageNode?): List<Node> {
        val children = mutableListOf<Node>()
        val newMessageNode = MessageNode(ctx.messageName.text, children, parentMessageNode)

        children.addAll(ctx.message().map { visitMessage(it, newMessageNode) }.flatten())
        children.addAll(ctx.proto_enum().map { protoEnum -> EnumNode(protoEnum.enumName.text, newMessageNode) })

        return listOf(newMessageNode)
    }
}