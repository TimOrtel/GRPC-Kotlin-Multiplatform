package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option

import kotlin.reflect.KClass

sealed interface OptionTargetMatcher {

    val target: KClass<out OptionTarget>

    fun matches(target: OptionTarget): MatchResult {
        return if (target::class == this.target) MatchResult.Success
        else MatchResult.Failure("Invalid option target")
    }

    sealed class TypeDeclaration(override val target: KClass<out OptionTarget>, val restrictToTopLevel: Boolean) :
        OptionTargetMatcher {
        override fun matches(target: OptionTarget): MatchResult {
            if (target !is OptionTarget.TypeDeclaration) return MatchResult.Failure("Invalid option target")

            return (!restrictToTopLevel || target.isTopLevel) asResult "Only applicable to top level but not applied to top level declaration"
        }
    }

    sealed class OtherDeclaration(override val target: KClass<out OptionTarget>) : OptionTargetMatcher

    data object FILE : OtherDeclaration(OptionTarget.FILE::class)
    data object EXTENSION_RANGE : OtherDeclaration(OptionTarget.EXTENSION_RANGE::class)
    class MESSAGE(restrictToTopLevel: Boolean = false) :
        TypeDeclaration(OptionTarget.MESSAGE::class, restrictToTopLevel)

    data class FIELD(val restriction: Restriction = Restriction.NoRestriction) :
        OtherDeclaration(OptionTarget.FIELD::class) {
        override fun matches(target: OptionTarget): MatchResult {
            if (target !is OptionTarget.FIELD) return MatchResult.Failure("Invalid option target")

            return when (restriction) {
                Restriction.NoRestriction -> MatchResult.Success
                is Restriction.OnlyOnRepeated -> when (target.type) {
                    OptionTarget.FIELD.Type.Map, OptionTarget.FIELD.Type.OneOf -> MatchResult.Failure("Only applicable to repeated fields, but applied to ${target.type}")
                    is OptionTarget.FIELD.Type.Regular -> target.type.isRepeated asResult "Only applicable to repeated fields but field is not repeated" and
                            ((!restriction.forcePackable || target.type.isPackable) asResult "Only applicable to packable field types but type is not packable.")
                }
            }
        }

        sealed interface Restriction {
            data object NoRestriction : Restriction
            data class OnlyOnRepeated(val forcePackable: Boolean) : Restriction
        }
    }

    data object ONEOF : OtherDeclaration(OptionTarget.ONEOF::class)

    class ENUM(restrictToTopLevel: Boolean = false) : TypeDeclaration(OptionTarget.ENUM::class, restrictToTopLevel)

    data object ENUM_ENTRY : OtherDeclaration(OptionTarget.ENUM_ENTRY::class)

    class SERVICE(restrictToTopLevel: Boolean = false) :
        TypeDeclaration(OptionTarget.SERVICE::class, restrictToTopLevel)

    data object METHOD : OtherDeclaration(OptionTarget.METHOD::class)

}

sealed interface MatchResult {
    data object Success : MatchResult
    data class Failure(val reason: String) : MatchResult
}

infix fun Boolean.asResult(failureString: String): MatchResult {
    return if (this) MatchResult.Success else MatchResult.Failure(failureString)
}

infix fun MatchResult.and(other: MatchResult): MatchResult {
    return when (this) {
        is MatchResult.Failure -> this
        MatchResult.Success -> other
    }
}
