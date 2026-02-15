package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.structure

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.DeclarationResolver
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoExtensionDefinition
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoExtensionDefinitionFinder
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoNode
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoProject
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoStructureDeclaration

data class ProtoPackage(val name: String, val packages: List<ProtoPackage>, val files: List<ProtoFile>) :
    DeclarationResolver, ProtoNode, ProtoExtensionDefinitionFinder {

    lateinit var parent: Parent

    val packageIdentifier: String
        get() = when (val p = parent) {
            is Parent.Package -> "${p.`package`.packageIdentifier}.$name"
            is Parent.Project -> ""
        }

    override val candidates: List<DeclarationResolver.Candidate>
        get() =
            files.flatMap { it.candidates } + packages.map { DeclarationResolver.Candidate.Package(it) }

    init {
        packages.forEach { it.parent = Parent.Package(this) }
        files.forEach { it.protoPackage = this }
    }

    override fun validate() {
        files.forEach { it.validate() }
        packages.forEach { it.validate() }
    }

    override fun resolveDeclarationInParent(type: ProtoType.DefType): ProtoStructureDeclaration? {
        return when (val p = parent) {
            is Parent.Package -> p.`package`.resolveDeclaration(type)
            // Base package reached, no more resolution possible
            is Parent.Project -> null
        }
    }

    override fun findExtensionDefinitions(): List<ProtoExtensionDefinition> {
        return packages.flatMap { it.findExtensionDefinitions() } + files.flatMap { it.findExtensionDefinitions() }
    }

    /**
     * Recursively finds all files that match the predicate
     */
    fun findFiles(predicate: (ProtoFile) -> Boolean): List<ProtoFile> {
        return files.filter(predicate) + packages.flatMap { it.findFiles(predicate) }
    }

    sealed interface Parent {
        data class Package(val `package`: ProtoPackage) : Parent
        data class Project(val project: ProtoProject) : Parent
    }
}
