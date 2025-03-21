package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.ProtoDeclaration

data class ProtoPackage(val name: String, val packages: List<ProtoPackage>, val files: List<ProtoFile>) :
    DeclarationResolver {

    lateinit var parent: Parent

    val packageIdentifier: String
        get() = when (val p = parent) {
            is Parent.Package -> "${p.`package`}.$name"
            is Parent.Project -> "."
        }

    override val candidates: List<DeclarationResolver.Candidate>
        get() =
            files.flatMap { it.candidates } + packages.map { DeclarationResolver.Candidate.Package(it) }

    init {
        packages.forEach { it.parent = Parent.Package(this) }
        files.forEach { it.protoPackage = this }
    }

    override fun resolveDeclarationInParent(type: ProtoType.DefType): ProtoDeclaration? {
        return when (val p = parent) {
            is Parent.Package -> p.`package`.resolveDeclaration(type)
            // Base package reached, no more resolution possible
            is Parent.Project -> null
        }
    }

    sealed interface Parent {
        data class Package(val `package`: ProtoPackage) : Parent
        data class Project(val project: ProtoProject) : Parent
    }
}
