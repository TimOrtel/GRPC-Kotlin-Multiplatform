package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoOneOf
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const

/**
 * Generated methods and the corresponding enum for an oneof field
 */
abstract class OneOfMethodAndClassGenerator {

    protected abstract val attrs: List<KModifier>

    fun generateMethodsAndClasses(builder: TypeSpec.Builder, protoMessage: ProtoMessage, protoOneOf: ProtoOneOf) {
        val enumClassName = Const.Message.OneOf.CaseEnum.oneOfCaseClassName(protoOneOf)
        val enumClassType = ClassName(protoMessage.pkg, protoMessage.commonName, enumClassName)

        builder.addType(
            TypeSpec
                .enumBuilder(enumClassName)
                .addModifiers(attrs)
                .addEnumConstant(Const.Message.OneOf.CaseEnum.EnumNotSet.name(protoOneOf))
                .apply {
                    protoOneOf.attributes.forEach { attr ->
                        addEnumConstant(Const.Message.OneOf.CaseEnum.EnumField.name(attr))
                    }
                }
                .build()
        )

        builder.addProperty(
            PropertySpec
                .builder(Const.Message.OneOf.propertyCaseName(protoOneOf), enumClassType.copy(nullable = true))
                .addModifiers(attrs)
                .apply { modifyGetCaseProperty(this, enumClassType, protoMessage, protoOneOf) }
                .build()
        )
    }

    protected abstract fun modifyGetCaseProperty(
        builder: PropertySpec.Builder,
        enumClassName: ClassName,
        message: ProtoMessage,
        oneOf: ProtoOneOf
    )
}