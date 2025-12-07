package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option

sealed interface OptionTarget {

    interface TypeDeclaration : OptionTarget {
        val isTopLevel: Boolean
    }

    interface OtherDeclaration : OptionTarget

    data object FILE : OtherDeclaration

    data object EXTENSION_RANGE : OtherDeclaration

    data class MESSAGE(override val isTopLevel: Boolean) : TypeDeclaration

    data class FIELD(val type: Type) : OtherDeclaration {
        sealed interface Type {
            data object Map : Type
            data object OneOf : Type
            data class Regular(val isRepeated: Boolean, val isPackable: Boolean) : Type
        }
    }

    data object ONEOF : OtherDeclaration

    data class ENUM(override val isTopLevel: Boolean) : TypeDeclaration

    data object ENUM_ENTRY : OtherDeclaration

    data object SERVICE : TypeDeclaration {
        override val isTopLevel: Boolean = true
    }

    data object METHOD : OtherDeclaration
}
