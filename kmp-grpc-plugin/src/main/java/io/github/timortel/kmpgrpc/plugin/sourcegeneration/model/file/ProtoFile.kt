package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file

import com.squareup.kotlinpoet.ClassName
import dev.turingcomplete.textcaseconverter.StandardTextCases
import dev.turingcomplete.textcaseconverter.TextCase
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.*
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoStructureDeclaration
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoEnum
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option.OptionTarget
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option.Options
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.service.ProtoService
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.structure.ProtoFolder
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.structure.ProtoPackage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.snakeCaseToCamelCase

data class ProtoFile(
    val `package`: String?,
    val fileName: String,
    val fileNameWithoutExtension: String,
    val languageVersion: ProtoLanguageVersion,
    override val messages: List<ProtoMessage>,
    val enums: List<ProtoEnum>,
    val services: List<ProtoService>,
    override val options: List<ProtoOption>,
    val imports: List<ProtoImport>,
    override val extensionDefinitions: List<ProtoExtensionDefinition>
) : FileBasedDeclarationResolver, ProtoOptionsHolder, ProtoVisibilityHolder, ProtoExtensionDefinitionHolder,
    ProtoExtensionDefinitionFinder, SourceCodeNamedNode {
    lateinit var folder: ProtoFolder
    lateinit var protoPackage: ProtoPackage

    /**
     * The path of this proto file relative to the root directory
     */
    val path: String
        get() {
            return if (folder.path != null) {
                "${folder.path}/$fileName"
            } else {
                fileName
            }
        }

    override val project: ProtoProject
        get() = folder.project

    override val file: ProtoFile
        get() = this

    override val candidates: List<DeclarationResolver.Candidate> =
        messages.map { DeclarationResolver.Candidate.Message(it) } + enums.map { DeclarationResolver.Candidate.Enum(it) }

    val javaPackage: String
        get() {
            return Options.Basic.javaPackage.get(this) ?: `package`.orEmpty()
        }

    val javaFileName: String
        get() {
            return Options.Basic.javaOuterClassName.get(this) ?: fileNameWithoutExtension.snakeCaseToCamelCase()
        }

    val className: ClassName get() = ClassName(javaPackage, javaFileName)

    override val optionTarget: OptionTarget = OptionTarget.FILE

    override val parentOptionsHolder: ProtoOptionsHolder? = null

    val topLevelDeclarations: List<SourceCodeNamedNode> = messages + enums + services + extensionDefinitions.flatMap { it.fields }

    val declarationCodeNameResolver = object : CodeNameResolver {
        override val reservedNames: Set<String> = emptySet()

        override val consideredNodes: List<SourceCodeNamedNode> = topLevelDeclarations
    }

    override val kotlinIdiomaticTextCase: TextCase
        get() = StandardTextCases.PASCAL_CASE

    override val name: String = fileName

    override val codeNameResolver: CodeNameResolver
        get() = project.getCodeNameResolverForKotlinPackage(javaPackage)

    val dslFile = DslFile(this)

    init {
        val parent = ProtoDeclParent.File(this)

        imports.forEach { it.file = this }
        services.forEach { it.file = this }
        messages.forEach { it.parent = parent }
        enums.forEach { it.parent = parent }
        extensionDefinitions.forEach { it.parent = ProtoExtensionDefinition.Parent.File(this) }
    }

    override fun resolveDeclarationInParent(type: ProtoType.DefType): ProtoStructureDeclaration? {
        return protoPackage.resolveDeclaration(type)
    }

    override fun validate() {
        super<FileBasedDeclarationResolver>.validate()
        super<ProtoOptionsHolder>.validate()
        super<ProtoExtensionDefinitionHolder>.validate()

        messages.forEach { it.validate() }
        enums.forEach { it.validate() }
        services.forEach { it.validate() }
        imports.forEach { it.validate() }
    }

    class DslFile(val file: ProtoFile) : SourceCodeNamedNode {
        override val project: ProtoProject
            get() = file.project

        override val kotlinIdiomaticTextCase: TextCase = StandardTextCases.PASCAL_CASE

        override val name: String = file.name
        override val desiredCodeName: String = "${name}Dsl"

        override val codeNameResolver: CodeNameResolver
            get() = file.codeNameResolver

        val className get() = ClassName(file.javaPackage, codeName)
    }
}
