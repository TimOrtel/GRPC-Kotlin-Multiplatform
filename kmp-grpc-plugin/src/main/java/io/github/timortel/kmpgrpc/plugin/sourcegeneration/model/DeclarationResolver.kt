package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.CompilationException
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoDeclaration
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoEnum
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoImport
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.structure.ProtoPackage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.type.ProtoType
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.toFilePositionString

/**
 * Implementation for resolving declarations in both messages and enums
 */
interface DeclarationResolver : BaseDeclarationResolver {

    val candidates: List<Candidate>

    override fun resolveDeclaration(type: ProtoType.DefType): ProtoDeclaration? {
        return resolveDeclaration(type, true)
    }

    /**
     * @param type the identifier of the declaration. Must not start with '.'.
     * @param canTryNextInnerScope if resolving may go to the parent to try the next inner scope
     */
    fun resolveDeclaration(type: ProtoType.DefType, canTryNextInnerScope: Boolean): ProtoDeclaration? {
        // Search scope
        val identifier = type.declaration

        val fileToImport = type.file.imports.associateBy { import ->
            type.file.project.rootFolder.resolveImport(import.path)
                ?: throw CompilationException.UnresolvedImport(
                    "Unable to resolve import ${import.identifier}",
                    type.file,
                    import.ctx
                )
        }

        // Only allow candidates from the file itself or from imported files
        val allowedCandidates = candidates.filter { candidate ->
            when (candidate) {
                is Candidate.Message, is Candidate.Enum -> {
                    when {
                        candidate.file == type.file -> true
                        else -> {
                            val import = fileToImport[candidate.file]
                            import != null && import.type == ProtoImport.Type.DEFAULT
                        }
                    }
                }
                is Candidate.Package -> true // Packages are always allowed
            }
        }

        return when {
            identifier.contains('.') -> {
                val firstIdentifier = identifier.substringBefore('.')
                val remainingIdentifier = identifier.substringAfter('.')

                val matchingCandidates = allowedCandidates.filter { it.name == firstIdentifier }

                validateCandidates(type, matchingCandidates)

                when {
                    matchingCandidates.isNotEmpty() -> {
                        val newType = type.copy(declaration = remainingIdentifier)

                        // Go deeper into the tree. No turning back. There must be exactly one element in the list.
                        when (val candidate = matchingCandidates.first()) {
                            is Candidate.Message -> candidate.message.resolveDeclaration(newType, false)
                            is Candidate.Enum -> candidate.enum.resolveDeclaration(newType)
                            is Candidate.Package -> candidate.pkg.resolveDeclaration(newType, false)
                        }
                    }

                    canTryNextInnerScope -> {
                        // Go higher up into the tree
                        resolveDeclarationInParent(type)
                    }

                    else -> null
                }
            }

            else -> {
                // The declaration must be in this scope
                val matchingCandidates = allowedCandidates.filter { it.name == identifier }

                validateCandidates(type, matchingCandidates)

                when {
                    matchingCandidates.isNotEmpty() -> when (val candidate = matchingCandidates.first()) {
                        is Candidate.Message -> candidate.message
                        is Candidate.Enum -> candidate.enum
                        is Candidate.Package -> throw CompilationException.ResolvedToPackage(
                            "Declaration was resolved to package, but message or enumeration was expected",
                            type.file,
                            type.ctx
                        )
                    }

                    canTryNextInnerScope -> {
                        // Go higher up into the tree
                        resolveDeclarationInParent(type)
                    }

                    else -> null
                }
            }
        }
    }

    /**
     * Tries resolving the identifier in the next inner scope, which is the parent of this node
     */
    fun resolveDeclarationInParent(type: ProtoType.DefType): ProtoDeclaration?

    /**
     * @param type the identifier relevant for the current scope
     */
    private fun validateCandidates(
        type: ProtoType.DefType,
        candidates: List<Candidate>
    ) {
        if (candidates.size > 1) {
            val message = buildString {
                append("Found clashing candidates for ${type.declaration}:")
                append(
                    candidates
                        .joinToString("\n") { candidate: Candidate ->
                            "-> ${candidate.name} at ${candidate.getLocation()}"
                        }
                )
            }

            throw CompilationException.ConflictingResolution(message, type.file, type.ctx)
        }
    }

    sealed interface Candidate {
        val name: String

        fun getLocation(): String

        sealed interface FileBasedCandidate : Candidate {
            val file: ProtoFile
        }

        data class Message(val message: ProtoMessage) : FileBasedCandidate {
            override val name: String
                get() = message.name

            override val file: ProtoFile
                get() = message.file

            override fun getLocation(): String = message.ctx.toFilePositionString(message.file.path)
        }

        data class Enum(val enum: ProtoEnum) : FileBasedCandidate {
            override val name: String
                get() = enum.name

            override val file: ProtoFile
                get() = enum.file

            override fun getLocation(): String = enum.ctx.toFilePositionString(enum.file.path)

        }

        data class Package(val pkg: ProtoPackage) : Candidate {
            override val name: String
                get() = pkg.name

            override fun getLocation(): String = "package ${pkg.packageIdentifier}"
        }
    }
}
