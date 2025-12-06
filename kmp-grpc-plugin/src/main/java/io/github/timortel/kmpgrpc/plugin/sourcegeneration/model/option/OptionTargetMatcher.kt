package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option

import kotlin.reflect.KClass

sealed interface OptionTargetMatcher {

    val target: KClass<out OptionTarget>

    sealed class TypeDeclaration(override val target: KClass<out OptionTarget>, val restrictToTopLevel: Boolean) :
        OptionTargetMatcher

    sealed class OtherDeclaration(override val target: KClass<out OptionTarget>) : OptionTargetMatcher

    data object FILE : OtherDeclaration(OptionTarget.FILE::class)
    data object EXTENSION_RANGE : OtherDeclaration(OptionTarget.EXTENSION_RANGE::class)
    class MESSAGE(restrictToTopLevel: Boolean = false) : TypeDeclaration(OptionTarget.MESSAGE::class, restrictToTopLevel)
    data object FIELD : OtherDeclaration(OptionTarget.FIELD::class)
    data object ONEOF : OtherDeclaration(OptionTarget.ONEOF::class)
    class ENUM(restrictToTopLevel: Boolean = false) : TypeDeclaration(OptionTarget.ENUM::class, restrictToTopLevel)
    data object ENUM_ENTRY : OtherDeclaration(OptionTarget.ENUM_ENTRY::class)
    class SERVICE(restrictToTopLevel: Boolean = false) : TypeDeclaration(OptionTarget.SERVICE::class, restrictToTopLevel)
    data object METHOD : OtherDeclaration(OptionTarget.METHOD::class)
}
