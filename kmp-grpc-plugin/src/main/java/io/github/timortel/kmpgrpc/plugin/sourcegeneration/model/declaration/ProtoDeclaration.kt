package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration

import com.squareup.kotlinpoet.ClassName
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoDeclParent

/**
 * Base interface of both messages and enums
 */
sealed interface ProtoDeclaration : ProtoBaseDeclaration {

    /**
     * The parent node of this declaration.
     */
    val parent: ProtoDeclParent

    /**
     * The type of this declaration as it will be generated
     */
    override val className: ClassName
        get() {
            return when (val p = parent) {
                is ProtoDeclParent.Message -> p.message.className.nestedClass(name)
                is ProtoDeclParent.File -> super.className
            }
        }

    override val isNested: Boolean
        get() = when (parent) {
            is ProtoDeclParent.Message -> true
            is ProtoDeclParent.File -> super.isNested
        }
}
