package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.capitalize
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.Options
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.ProtoDeclParent
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.ProtoFile
import org.antlr.v4.runtime.ParserRuleContext

sealed interface ProtoDeclaration {

    /**
     * The name of this declaration
     */
    val name: String

    /**
     * The parent node of this declaration.
     */
    val parent: ProtoDeclParent

    /**
     * The file this declaration is located in
     */
    val file: ProtoFile

    /**
     * The type of this declaration as it will be generated
     */
    val typeName: TypeName
        get() {
            return if (Options.javaUseMultipleFiles.get(file, file.options)) {
                ClassName(file.`package`.orEmpty(), name)
            } else {
                ClassName(file.`package`.orEmpty(), listOf(file.fileNameWithoutExtension.capitalize(), name))
            }
        }

    val ctx: ParserRuleContext
}
