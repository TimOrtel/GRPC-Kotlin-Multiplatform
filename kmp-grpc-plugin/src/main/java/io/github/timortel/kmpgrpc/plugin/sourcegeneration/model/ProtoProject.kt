package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoDeclaration
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.structure.ProtoFolder
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.structure.ProtoPackage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType
import org.slf4j.Logger

data class ProtoProject(
    val rootFolder: ProtoFolder,
    val logger: Logger,
    val defaultVisibility: Visibility
) : ProtoNode, ProtoExtensionDefinitionFinder {

    val rootPackage: ProtoPackage

    // This is an optimization, to avoid searching the tree for every message
    private val extensionDefinitionsMap: Map<ProtoDeclaration, List<ProtoExtensionDefinition>>

    init {
        rootFolder.parent = ProtoFolder.Parent.Project(this)

        rootPackage = buildPackageTree(rootFolder.collectFiles(), emptyList())
        rootPackage.parent = ProtoPackage.Parent.Project(this)

        extensionDefinitionsMap = findExtensionDefinitions().groupBy { it.messageType.resolveDeclaration() }
    }

    override fun validate() {
        rootPackage.validate()
    }

    fun resolveDeclarationFullyQualified(type: ProtoType.DefType): ProtoDeclaration? {
        return rootPackage.resolveDeclaration(type)
    }

    override fun findExtensionDefinitions(): List<ProtoExtensionDefinition> {
        return rootPackage.findExtensionDefinitions()
    }

    fun findExtensionDefinitionsForMessage(message: ProtoMessage): List<ProtoExtensionDefinition> {
        return extensionDefinitionsMap[message].orEmpty()
    }

    private fun buildPackageTree(
        files: List<ProtoFile>,
        parentPackages: List<String>
    ): ProtoPackage {
        val currentPackage = parentPackages.joinToString(".") { it }

        val packageGroups = files.groupBy { file ->
            val filePackage = file.`package`.orEmpty()
            val relativePackage = filePackage.substring(startIndex = currentPackage.length)

            when {
                relativePackage.isEmpty() -> null
                relativePackage.startsWith('.') -> relativePackage.substring(1).substringBefore('.')
                else -> relativePackage.substringBefore('.')
            }
        }

        val packageFiles = packageGroups[null].orEmpty()

        val subPackages = packageGroups.mapNotNull { (packageName, files) ->
            if (packageName == null) {
                null
            } else {
                buildPackageTree(files, parentPackages + packageName)
            }
        }

        return ProtoPackage(parentPackages.lastOrNull().orEmpty(), subPackages, packageFiles)
    }
}
