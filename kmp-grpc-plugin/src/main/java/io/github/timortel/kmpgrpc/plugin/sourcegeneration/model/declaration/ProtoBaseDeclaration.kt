package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration

import com.squareup.kotlinpoet.ClassName
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.Options
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoOptionsHolder
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import org.antlr.v4.runtime.ParserRuleContext

interface ProtoBaseDeclaration : ProtoOptionsHolder {

    /**
     * The name of this declaration
     */
    val name: String

    /**
     * The name of the class generated for this declaration
     */
    val kotlinClassName: String get() = name

    /**
     * The file this declaration is located in
     */
    override val file: ProtoFile

    /**
     * The type of this declaration as it will be generated
     */
    val className: ClassName
        get() {
            return if (Options.javaMultipleFiles.get(file)) {
                ClassName(file.javaPackage, kotlinClassName)
            } else {
                file.className.nestedClass(kotlinClassName)
            }
        }

    /**
     * If the message is nested within another class in the generated code.
     */
    val isNested: Boolean
        get() = !Options.javaMultipleFiles.get(file)

    val ctx: ParserRuleContext
}
