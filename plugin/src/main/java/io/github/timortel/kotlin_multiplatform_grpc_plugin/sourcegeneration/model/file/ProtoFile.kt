package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.file

import com.squareup.kotlinpoet.ClassName
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.Protobuf3CompilationException
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.ProtoDeclaration
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.ProtoEnum
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.service.ProtoService
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.structure.ProtoFolder
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.structure.ProtoPackage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.util.snakeCaseToCamelCase

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

    val javaPackage: String get() {
        return Options.javaPackage.get(this) ?: `package`.orEmpty()
    }

    val javaFileName: String get() {
        return Options.javaOuterClassName.get(this) ?: fileNameWithoutExtension.snakeCaseToCamelCase()
    }

    val className: ClassName get() = ClassName(javaPackage, javaFileName)

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
