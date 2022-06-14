package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.common

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const

abstract class CommonFunctionGenerator {

    /**
     * Recursively adds all common getters. Common getters transform the native values to common values.
     */
    fun generateCommonGetter(messages: List<ProtoMessage>) {
        messages.forEach { message ->
            //Common property, that converts this JS type to a common type
            addFunction(
                FunSpec
                    .builder(Const.Message.CommonFunction.NAME)
                    .addParameter(
                        Const.Message.CommonFunction.PARAMETER_NATIVE,
                        getNativePlatformType(message)
                    )
                    .addCode(getGetter(message))
                    .returns(message.commonType)
                    .build()
            )

            generateCommonGetter(message.children)
        }
    }

    private fun getGetter(message: ProtoMessage): CodeBlock =
        CodeBlock.builder()
            .add("return·%T(%N)", message.commonType, Const.Message.CommonFunction.PARAMETER_NATIVE)
            .build()

    protected abstract fun addFunction(funSpec: FunSpec)

    /**
     * @return the type of the message for the native type, so for JVM returns message.jvmType
     */
    protected abstract fun getNativePlatformType(message: ProtoMessage): TypeName

}