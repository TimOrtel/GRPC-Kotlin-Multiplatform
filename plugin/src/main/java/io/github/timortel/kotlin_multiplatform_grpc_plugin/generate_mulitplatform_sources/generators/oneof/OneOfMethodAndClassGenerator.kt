package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof

import com.squareup.kotlinpoet.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessageAttribute
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoOneOf
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const

/**
 * Generated methods and the corresponding enum for an oneof field
 */
abstract class OneOfMethodAndClassGenerator(private val isActual: Boolean) {

    protected abstract val attrs: List<KModifier>

    fun generateMethodsAndClasses(builder: TypeSpec.Builder, protoMessage: ProtoMessage, protoOneOf: ProtoOneOf) {
        val parentSealedClassName = Const.Message.OneOf.parentSealedClassName(protoMessage, protoOneOf)

        builder.addProperty(
            PropertySpec
                .builder(
                    Const.Message.OneOf.propertyName(protoMessage, protoOneOf),
                    parentSealedClassName,
                    attrs
                )
                .apply { modifyOneOfProperty(this, protoMessage, protoOneOf) }
                .build()
        )

        builder.addType(
            TypeSpec
                .classBuilder(parentSealedClassName)
                .addModifiers(KModifier.SEALED)
                .addModifiers(attrs)
                .apply {
                    modifyParentClass(this, protoMessage, protoOneOf)

                    protoOneOf.attributes.forEach { attr ->
                        val propName = Const.Message.Attribute.propertyName(protoMessage, attr)

                        addType(
                            TypeSpec
                                .classBuilder(Const.Message.OneOf.childClassName(protoMessage, protoOneOf, attr))
                                .addModifiers(attrs)
                                .apply {
                                    if (isActual) {
                                        superclass(parentSealedClassName)
                                        primaryConstructor(
                                            FunSpec
                                                .constructorBuilder()
                                                .addParameter(
                                                    ParameterSpec
                                                        .builder(propName, attr.commonType)
                                                        .defaultValue(attr.commonDefaultValue(false))
                                                        .build()
                                                )
                                                .build()
                                        )
                                    } else {
                                        addSuperinterface(parentSealedClassName)
                                    }

                                    modifyChildClass(this, protoMessage, protoOneOf, ChildClassType.Normal(attr))
                                }
                                .addProperty(
                                    PropertySpec
                                        .builder(
                                            Const.Message.Attribute.propertyName(protoMessage, attr),
                                            attr.commonType
                                        )
                                        .apply {
                                            if (isActual) {
                                                initializer(Const.Message.Attribute.propertyName(protoMessage, attr))
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
                                        superclass(parentSealedClassName)
                                    } else {
                                        addSuperinterface(parentSealedClassName)
                                    }
                                    modifyChildClass(this, protoMessage, protoOneOf, type)
                                }
                                .build()
                        )
                    }

                    //Unknown
                    addObject(Const.Message.OneOf.unknownClassName(protoMessage, protoOneOf), ChildClassType.Unknown)
                    addObject(Const.Message.OneOf.notSetClassName(protoMessage, protoOneOf), ChildClassType.NotSet)
                }
                .build()
        )
    }

    protected abstract fun modifyOneOfProperty(
        builder: PropertySpec.Builder,
        message: ProtoMessage,
        oneOf: ProtoOneOf
    )

    protected open fun modifyParentClass(builder: TypeSpec.Builder, message: ProtoMessage, oneOf: ProtoOneOf) {}
    protected open fun modifyChildClass(
        builder: TypeSpec.Builder,
        message: ProtoMessage,
        oneOf: ProtoOneOf,
        childClassType: ChildClassType
    ) {}

    sealed class ChildClassType {
        class Normal(val attr: ProtoMessageAttribute) : ChildClassType()
        object NotSet : ChildClassType()
        object Unknown : ChildClassType()
    }
}