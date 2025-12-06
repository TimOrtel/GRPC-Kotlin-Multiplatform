package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option

sealed interface OptionTarget {

    interface TypeDeclaration : OptionTarget {
        val isTopLevel: Boolean
    }

    interface OtherDeclaration : OptionTarget

    data object FILE : OtherDeclaration
    data object EXTENSION_RANGE : OtherDeclaration
    data class MESSAGE(override val isTopLevel: Boolean) : TypeDeclaration
    data object FIELD : OtherDeclaration
    data object ONEOF : OtherDeclaration
    data class ENUM(override val isTopLevel: Boolean) : TypeDeclaration
    data object ENUM_ENTRY : OtherDeclaration

    data object SERVICE : TypeDeclaration {
        override val isTopLevel: Boolean = true
    }

    data object METHOD : OtherDeclaration
}
