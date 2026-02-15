package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message

import dev.turingcomplete.textcaseconverter.StandardTextCases
import io.github.timortel.kmpgrpc.plugin.NamingStrategy
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.SourceCodeNamedNode
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.util.capitalize

interface OneOfSealedClassNameHolder : SourceCodeNamedNode {

    val sealedClassRawName: String
        get() = when (project.namingStrategy) {
            NamingStrategy.PROTO_LITERAL -> transformedKotlinName.capitalize()
            NamingStrategy.KOTLIN_IDIOMATIC -> kotlinIdiomaticTextCase.convertTo(
                StandardTextCases.PASCAL_CASE,
                transformedKotlinName
            )
        }
}
