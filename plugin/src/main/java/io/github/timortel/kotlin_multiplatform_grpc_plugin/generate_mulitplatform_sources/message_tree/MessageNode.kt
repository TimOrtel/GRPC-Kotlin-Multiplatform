package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.message_tree

sealed class Node(val name: String, val parent: MessageNode?) {
    val path: List<MessageNode> = parent?.path.orEmpty() + if (parent != null) listOf(parent) else emptyList()
}

class MessageNode(name: String, val children: List<Node>, parent: MessageNode?): Node(name, parent) {

    override fun toString(): String {
        return "MessageNode(name=$name, children=${children.map { it.name }}, parent=$parent)"
    }
}

class EnumNode(name: String, parent: MessageNode?) : Node(name, parent)