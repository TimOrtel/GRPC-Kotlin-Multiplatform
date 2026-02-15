package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model

import dev.turingcomplete.textcaseconverter.StandardTextCases
import dev.turingcomplete.textcaseconverter.TextCase
import io.github.timortel.kmpgrpc.plugin.NamingStrategy

/**
 * Base interface for all proto nodes that contain a name that could be written to code
 */
interface SourceCodeNamedNode {

    val project: ProtoProject

    val kotlinIdiomaticTextCase: TextCase

    /**
     * The name of the property as defined in the proto source code
     */
    val name: String

    /**
     * The name of the property after applying the code style transformation rules.
     */
    val transformedKotlinName: String
        get() = when (project.namingStrategy) {
            NamingStrategy.PROTO_LITERAL -> name
            NamingStrategy.KOTLIN_IDIOMATIC -> {
                // Ensure the input is treated as one cohesive "word" set before converting
                val normalizedName = name.trim('_') // Protobuf allows __internal__ names

                when {
                    // If it's pure uppercase with underscores (likely an Enum constant)
                    normalizedName.all { it.isUpperCase() || it == '_' || it.isDigit() } ->
                        StandardTextCases.SCREAMING_SNAKE_CASE.convertTo(kotlinIdiomaticTextCase, normalizedName)

                    // If it has underscores, it's a variation of snake_case
                    normalizedName.contains('_') ->
                        StandardTextCases.SNAKE_CASE.convertTo(kotlinIdiomaticTextCase, normalizedName)

                    // Otherwise, assume it's already some form of Camel/Pascal case
                    else -> StandardTextCases.PASCAL_CASE.convertTo(kotlinIdiomaticTextCase, normalizedName)
                }
            }
        }

    /**
     * The name the node would like to have if there were not any clashes
     */
    val desiredCodeName: String get() = transformedKotlinName

    /**
     * The name of the node in the generated code
     */
    val codeName: String get() = codeNameResolver.resolveCodeName(this)

    /**
     * The priority of this node in the context of name clash resolution.
     */
    val priority: Int get() = 1

    val codeNameResolver: CodeNameResolver
}
