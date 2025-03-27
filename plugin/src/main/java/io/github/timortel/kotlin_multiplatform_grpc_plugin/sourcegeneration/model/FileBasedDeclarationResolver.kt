package io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model

import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.CompilationException
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.DeclarationResolver.Candidate
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.model.file.ProtoFile

interface FileBasedDeclarationResolver : DeclarationResolver, ProtoNode {

    val file: ProtoFile

    override fun validate() {
        candidates
            .groupBy { it.name }
            .filter { it.value.size > 1 }
            .forEach { (name, candidates) ->
                val message = buildString {
                    append("Found clashing declarations for $name:")
                    append(
                        candidates
                            .joinToString("\n") { candidate: Candidate ->
                                "-> ${candidate.name} at ${candidate.getLocation()}"
                            }
                    )
                }

                throw CompilationException.DuplicateDeclaration(message, file)
            }
    }
}
