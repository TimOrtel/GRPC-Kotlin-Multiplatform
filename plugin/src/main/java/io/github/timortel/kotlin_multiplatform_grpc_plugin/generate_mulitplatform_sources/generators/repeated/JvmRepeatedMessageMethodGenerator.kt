package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.repeated

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessageAttribute
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const

object JvmRepeatedMessageMethodGenerator : RepeatedMessageMethodGenerator(true) {
    override val attrs: List<KModifier> = listOf(KModifier.ACTUAL)

    override fun getType(messageAttribute: ProtoMessageAttribute): TypeName = messageAttribute.commonType

    override fun modifyListProperty(builder: PropertySpec.Builder, message: ProtoMessage, attr: ProtoMessageAttribute) {
        if (attr.types.isEnum) {
            builder.initializer(
                "%N.get${attr.capitalizedName}ValueList().map(%T::%N)",
                Const.Message.Constructor.JVM.PARAM_IMPL,
                attr.commonType,
                Const.Enum.getEnumForNumFunctionName
            )
        } else if (attr.types.doDiffer) {
            builder.initializer(
                "%N.${attr.name}List.map·{ %M(it) }",
                Const.Message.Constructor.JVM.PARAM_IMPL,
                Const.Message.CommonFunction.JVM.commonFunction(attr)
            )
        } else {
            builder.initializer("%N.${attr.name}List", Const.Message.Constructor.JVM.PARAM_IMPL)
        }
    }

}