package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.ios

import com.squareup.kotlinpoet.CodeBlock
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.ProtoType
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessageAttribute
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const

object IOSDefaultAttributeValue {
    fun getDefaultValueForAttr(protoMessageAttribute: ProtoMessageAttribute): CodeBlock {
        return when (protoMessageAttribute.types.protoType) {
            ProtoType.DOUBLE -> CodeBlock.of("0.0")
            ProtoType.FLOAT -> CodeBlock.of("0f")
            ProtoType.INT_32 -> CodeBlock.of("0")
            ProtoType.INT_64 -> CodeBlock.of("0L")
            ProtoType.BOOL -> CodeBlock.of("false")
            ProtoType.STRING -> CodeBlock.of("\"\"")
            ProtoType.MAP -> CodeBlock.of("emptyMap()")
            ProtoType.MESSAGE -> CodeBlock.of("null")
            ProtoType.ENUM -> {
                CodeBlock.of(
                    "%T.%N(0)",
                    protoMessageAttribute.types.iosType,
                    Const.Enum.getEnumForNumFunctionName
                )
            }
        }
    }
}