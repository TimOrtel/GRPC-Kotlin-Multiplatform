package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.message_tree

import io.github.timortel.kmpgrpc.anltr.Protobuf3BaseVisitor
import io.github.timortel.kmpgrpc.anltr.Protobuf3Parser

class Proto3MessageTreeBuilder : Protobuf3BaseVisitor<List<Node>>() {

    override fun visitProto(ctx: Protobuf3Parser.ProtoContext): List<Node> {
        ctx.topLevelDef()

        val messages = ctx
            .topLevelDef()
            .mapNotNull { it.messageDef() }
            .map { visitMessage(it, null) }
            .flatten()

        val enums = ctx.topLevelDef()
            .mapNotNull { it.enumDef() }
            .map { protoEnum -> EnumNode(protoEnum.enumName().text, null) }


        return messages + enums
    }

    private fun visitMessage(ctx: Protobuf3Parser.MessageDefContext, parentMessageNode: MessageNode?): List<Node> {
        val children = mutableListOf<Node>()
        val newMessageNode = MessageNode(ctx.messageName().text, children, parentMessageNode)

        val childMessages = ctx.messageBody().messageElement().mapNotNull { it.messageDef() }
        val childEnum = ctx.messageBody().messageElement().mapNotNull { it.enumDef() }

        children.addAll(childMessages.map { visitMessage(it, newMessageNode) }.flatten())
        children.addAll(childEnum.map { protoEnum -> EnumNode(protoEnum.enumName().text, newMessageNode) })

        return listOf(newMessageNode)
    }
}