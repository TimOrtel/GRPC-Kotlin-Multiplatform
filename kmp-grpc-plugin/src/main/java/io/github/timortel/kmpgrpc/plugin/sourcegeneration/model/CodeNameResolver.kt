package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model

/**
 * Used to resolve the property name of a proto node in the generated code and to avoid name clashes.
 */
interface CodeNameResolver {

    val reservedNames: Set<String>

    val consideredNodes: List<SourceCodeNamedNode>

    fun resolveNameConflict(name: String): String = "${name}_"

    fun resolveCodeName(field: SourceCodeNamedNode): String {
        val reservedNames = reservedNames.toMutableSet()
        val nameMap: MutableMap<SourceCodeNamedNode, String> = mutableMapOf()

        consideredNodes
            .sortedBy { it.priority }
            .forEach { currentNode ->
                var attributeName = currentNode.desiredCodeName

                while (attributeName in reservedNames) {
                    attributeName = resolveNameConflict(attributeName)
                }

                if (currentNode == field) return attributeName

                reservedNames += attributeName
                nameMap[currentNode] = attributeName
            }

        throw IllegalArgumentException("field=$field not child of resolver=$this. Known children=$consideredNodes.")
    }
}
