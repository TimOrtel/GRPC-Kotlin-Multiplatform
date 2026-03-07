package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model

/**
 * Used to resolve the property name of a proto node in the generated code and to avoid name clashes.
 */
interface CodeNameResolver {

    val reservedNames: Set<String>

    val consideredNodes: List<SourceCodeNamedNode>

    val conflictResolutionStrategy: ConflictResolutionStrategy get() = ConflictResolutionStrategy.appendUnderscore

    fun resolveCodeName(field: SourceCodeNamedNode): String {
        val reservedNames = reservedNames.toMutableSet()
        val nameMap: MutableMap<SourceCodeNamedNode, String> = mutableMapOf()

        consideredNodes
            .sortedBy { it.priority }
            .forEach { currentNode ->
                var attributeName = currentNode.desiredCodeName

                var conflictCount = 1
                while (attributeName in reservedNames) {
                    attributeName = conflictResolutionStrategy.resolveNameConflict(currentNode.desiredCodeName, attributeName, conflictCount)
                    conflictCount++
                }

                if (currentNode == field) return attributeName

                reservedNames += attributeName
                nameMap[currentNode] = attributeName
            }

        throw IllegalArgumentException("field=$field not child of resolver=$this. Known children=$consideredNodes.")
    }

    interface ConflictResolutionStrategy {
        fun resolveNameConflict(originalName: String, currentName: String, conflictCount: Int): String = "${currentName}_"

        companion object {
            val appendUnderscore: ConflictResolutionStrategy = object : ConflictResolutionStrategy {
                override fun resolveNameConflict(
                    originalName: String,
                    currentName: String,
                    conflictCount: Int
                ): String = "${currentName}_"
            }

            val appendNumber: ConflictResolutionStrategy = object : ConflictResolutionStrategy {
                override fun resolveNameConflict(
                    originalName: String,
                    currentName: String,
                    conflictCount: Int
                ): String = "${currentName}$conflictCount"
            }
        }
    }
}
