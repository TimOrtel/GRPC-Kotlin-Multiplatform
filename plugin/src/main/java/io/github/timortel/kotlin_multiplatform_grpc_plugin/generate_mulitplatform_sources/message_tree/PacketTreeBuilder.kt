package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.message_tree

import io.github.timortel.kotlin_multiplatform_grpc_plugin.anltr.Proto3Lexer
import io.github.timortel.kotlin_multiplatform_grpc_plugin.anltr.Proto3Parser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File

object PacketTreeBuilder {

    /**
     * @return the root package node
     */
    fun buildPacketTree(protoFiles: List<File>): PackageNode {
        val nodes = protoFiles.map { protoFile ->
            val lexer = Proto3Lexer(CharStreams.fromStream(protoFile.inputStream()))

            val parser = Proto3Parser(CommonTokenStream(lexer))

            val file = parser.file()
            val messageNodes = Proto3MessageTreeBuilder().visit(file)
            val fullPackage = file.proto_package().firstOrNull()?.pkgName?.text ?: ""

            var remainingPackageString = fullPackage
            var child: PackageNode? = null
            while (remainingPackageString.isNotEmpty()) {
                val packageString = remainingPackageString.substringAfterLast('.')
                remainingPackageString = remainingPackageString.substringBeforeLast('.', missingDelimiterValue = "")

                val messages = if (child == null) messageNodes else emptyList()

                child = PackageNode(packageString, messages, if (child == null) emptyList() else listOf(child))
            }

            child ?: PackageNode("", messageNodes, emptyList())
        }

        //messages without a package
        val topLevelMessages = nodes.filter { it.packageName.isEmpty() }.map { it.nodes }.flatten()
        val topLevelEnums = nodes.filter { it.packageName.isEmpty() }.map { it.nodes }

        //Merge the nodes
        val mergedNodes = mergeTree(nodes.filter { it.packageName.isNotEmpty() })

        return PackageNode("", topLevelMessages, mergedNodes)
    }

    private fun mergeTree(nodesInSameLevel: List<PackageNode>): List<PackageNode> {
        return nodesInSameLevel.groupBy { it.packageName }
            .map { (packageName, nodes) ->
                val combinedMessages = nodes.map { it.nodes }.flatten()
                val combined = nodes.map { it.children }.flatten()

                val merged = mergeTree(combined)
                PackageNode(packageName, combinedMessages, merged)
            }
    }
}