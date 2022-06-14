package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.message_tree

data class PackageNode(val packageName: String, val nodes: List<Node>, val children: List<PackageNode>)