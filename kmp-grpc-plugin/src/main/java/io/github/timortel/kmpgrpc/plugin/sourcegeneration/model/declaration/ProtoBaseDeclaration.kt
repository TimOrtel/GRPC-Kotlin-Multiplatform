package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration

import com.squareup.kotlinpoet.ClassName
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoLanguageVersion
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option.Options
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoOptionsHolder
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoProject
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoVisibilityHolder
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option.ProtoNestInFileClass
import org.antlr.v4.runtime.ParserRuleContext

interface ProtoBaseDeclaration : ProtoOptionsHolder, ProtoVisibilityHolder {

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

    override val project: ProtoProject
        get() = file.project

    /**
     * The type of this declaration as it will be generated
     */
    val className: ClassName
        get() {
            return if (isNested) {
                file.className.nestedClass(kotlinClassName)
            } else {
                ClassName(file.javaPackage, kotlinClassName)
            }
        }

    /**
     * If the message is nested within another class in the generated code.
     */
    val isNested: Boolean
        get() = when (file.languageVersion) {
            ProtoLanguageVersion.PROTO2, ProtoLanguageVersion.PROTO3, ProtoLanguageVersion.EDITION2023 -> !Options.Basic.javaMultipleFiles.get(file)
            ProtoLanguageVersion.EDITION2024 -> when (Options.Feature.nestInFileClass.get(this)) {
                ProtoNestInFileClass.YES -> true
                ProtoNestInFileClass.NO -> false
                ProtoNestInFileClass.LEGACY -> !Options.Basic.javaMultipleFiles.get(file)
            }
        }

    val ctx: ParserRuleContext
}
