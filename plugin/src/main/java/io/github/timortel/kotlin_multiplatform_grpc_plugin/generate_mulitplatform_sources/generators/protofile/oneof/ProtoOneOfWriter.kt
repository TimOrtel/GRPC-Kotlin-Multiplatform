package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.protofile.oneof

import com.squareup.kotlinpoet.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.ProtoOneOf
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.model.declaration.message.field.ProtoOneOfField

/**
 * Generated methods and the corresponding enum for an oneof field
 */
abstract class ProtoOneOfWriter(private val isActual: Boolean) {

    protected abstract val attrs: List<KModifier>

    fun generateMethodsAndClasses(builder: TypeSpec.Builder, protoOneOf: ProtoOneOf) {
        builder.addProperty(
            PropertySpec
                .builder(
                    name = Const.Message.OneOf.propertyName(protoOneOf),
                    type = protoOneOf.sealedClassName,
                    modifiers = attrs
                )
                .apply { modifyOneOfProperty(this, protoOneOf) }
                .build()
        )

        builder.addType(
            TypeSpec
                .classBuilder(protoOneOf.sealedClassName)
                .addModifiers(KModifier.SEALED)
                .addModifiers(attrs)
                .apply {
                    modifyParentClass(this, protoOneOf)

                    protoOneOf.fields.forEach { field ->
                        addType(
                            TypeSpec
                                .classBuilder(field.sealedClassChildName)
                                .addModifiers(attrs)
                                .apply {
                                    if (isActual) {
                                        superclass(protoOneOf.sealedClassName)
                                        addModifiers(KModifier.DATA)
                                    } else {
                                        addSuperinterface(protoOneOf.sealedClassName)
                                    }

                                    primaryConstructor(
                                        FunSpec
                                            .constructorBuilder()
                                            .addParameter(
                                                ParameterSpec
                                                    .builder(field.fieldName, field.type.resolve())
                                                    .build()
                                            )
                                            .apply {
                                                if (isActual) addModifiers(KModifier.ACTUAL)
                                            }
                                            .build()
                                    )

                                    modifyChildClass(this, protoOneOf, ChildClassType.Normal(field))
                                }
                                .addProperty(
                                    PropertySpec
                                        .builder(name = field.fieldName, type = field.type.resolve())
                                        .apply {
                                            if (isActual) {
                                                initializer(field.fieldName)
                                                addModifiers(KModifier.ACTUAL)
                                            }
                                        }
                                        .build()
                                )
                                .build()
                        )
                    }

                    val addObject = { name: ClassName, type: ChildClassType ->
                        addType(
                            TypeSpec
                                .objectBuilder(name)
                                .addModifiers(attrs)
                                .apply {
                                    if (isActual) {
                                        superclass(protoOneOf.sealedClassName)
                                    } else {
                                        addSuperinterface(protoOneOf.sealedClassName)
                                    }
                                    modifyChildClass(this, protoOneOf, type)
                                }
                                .build()
                        )
                    }

                    // Unknown
                    addObject(protoOneOf.sealedClassNameUnknown, ChildClassType.Unknown)
                    addObject(protoOneOf.sealedClassNameNotSet, ChildClassType.NotSet)
                }
                .build()
        )
    }

    protected abstract fun modifyOneOfProperty(builder: PropertySpec.Builder, oneOf: ProtoOneOf)

    protected open fun modifyParentClass(builder: TypeSpec.Builder, oneOf: ProtoOneOf) {}

    protected open fun modifyChildClass(builder: TypeSpec.Builder, oneOf: ProtoOneOf, childClassType: ChildClassType) =
        Unit

    sealed class ChildClassType {
        class Normal(val field: ProtoOneOfField) : ChildClassType()
        data object NotSet : ChildClassType()
        data object Unknown : ChildClassType()
    }
}