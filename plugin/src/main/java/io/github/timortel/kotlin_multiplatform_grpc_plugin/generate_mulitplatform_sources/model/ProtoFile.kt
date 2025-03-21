package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model

import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.Protobuf3CompilationException
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.ProtoDeclaration
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.ProtoEnum
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.service.ProtoService

data class ProtoFile(
    val `package`: String?,
    val fileName: String,
    val fileNameWithoutExtension: String,
    val messages: List<ProtoMessage>,
    val enums: List<ProtoEnum>,
    val services: List<ProtoService>,
    val options: List<ProtoOption>,
    val imports: List<ProtoImport>
) : DeclarationResolver {
    lateinit var folder: ProtoFolder
    lateinit var protoPackage: ProtoPackage

    /**
     * The path of this proto file relative to the root directory
     */
    val path: String
        get() {
            return folder.path + fileName
        }

    val project: ProtoProject
        get() = folder.project

    override val candidates: List<DeclarationResolver.Candidate> =
        messages.map { DeclarationResolver.Candidate.Message(it) } + enums.map { DeclarationResolver.Candidate.Enum(it) }

    val importedFiles: List<ProtoFile>
        get() = imports.map { import ->
            project.rootFolder.resolveImport(import.path)
                ?: throw Protobuf3CompilationException("Unable to resolve import ${import.identifier}", this, import.ctx)
        }

    init {
        val parent = ProtoDeclParent.File(this)

        services.forEach { it.file = this }
        messages.forEach { it.parent = parent }
        enums.forEach { it.parent = parent }
    }

    override fun resolveDeclarationInParent(type: ProtoType.DefType): ProtoDeclaration? {
        return protoPackage.resolveDeclaration(type)
    }
}
