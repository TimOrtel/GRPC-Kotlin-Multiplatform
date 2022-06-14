package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoOneOf
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const

object JvmOneOfMethodAndClassGenerator : OneOfMethodAndClassGenerator() {

    override val attrs: List<KModifier> = listOf(KModifier.ACTUAL)

    override fun modifyGetCaseProperty(
        builder: PropertySpec.Builder,
        enumClassName: ClassName,
        message: ProtoMessage,
        oneOf: ProtoOneOf
    ) {
        val implName = Const.Message.Constructor.JVM.PARAM_IMPL

        val jvmProtoEnumClassName = "${oneOf.capitalizedName}Case"
        val commonProtoEnumClassName = Const.Message.OneOf.CaseEnum.oneOfCaseClassName(oneOf)

        builder.getter(
            FunSpec
                .getterBuilder()
                .apply {
                    addCode("return when(%N.get${oneOf.name.capitalize()}Case()) {\n", implName)
                    //simply map each enum to the km enum
                    oneOf.attributes.forEach { attr ->
                        addCode(
                            "%T.%N.%N -> %T.%N.%N\n",
                            //JVM Builder types
                            message.jvmType, jvmProtoEnumClassName, attr.name.uppercase(),
                            //Generated KM types
                            message.commonType, commonProtoEnumClassName, Const.Message.OneOf.CaseEnum.EnumField.name(attr),
                        )
                    }
                    //Add case for when it's not set
                    addCode(
                        "%T.%N.%N -> %T.%N.%N\n",
                        message.jvmType, jvmProtoEnumClassName, "${oneOf.name.uppercase()}_NOT_SET",
                        message.commonType, commonProtoEnumClassName, Const.Message.OneOf.CaseEnum.EnumNotSet.name(oneOf)
                    )
                    //Unknown case
                    addCode("else -> null")
                    addCode("}")
                }
                .build()
        )
    }
}