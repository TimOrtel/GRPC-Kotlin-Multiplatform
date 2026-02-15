package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration

import com.squareup.kotlinpoet.TypeName
import dev.turingcomplete.textcaseconverter.StandardTextCases
import dev.turingcomplete.textcaseconverter.TextCase
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.SourceCodeNamedNode

/**
 * Base interface of a proto property of a generated class.
 */
interface ProtoChildProperty : SourceCodeNamedNode {

    override val kotlinIdiomaticTextCase: TextCase
        get() = StandardTextCases.SOFT_CAMEL_CASE

    /**
     * The type of the property in the generated code
     */
    val propertyType: TypeName
}
