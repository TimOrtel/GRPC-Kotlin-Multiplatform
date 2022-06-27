package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.proto_file

import com.squareup.kotlinpoet.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.JsMapMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.MapMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof.JsOneOfMethodAndClassGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof.OneOfMethodAndClassGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.repeated.JsRepeatedMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.repeated.RepeatedMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.scalar.JsScalarMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.scalar.ScalarMessageMethodGenerator

class JsProtoFileWriter(private val protoFile: ProtoFile) : ProtoFileWriter(protoFile, true), DefaultChildClassName {
    override val scalarMessageMethodGenerator: ScalarMessageMethodGenerator = JsScalarMessageMethodGenerator

    override val repeatedMessageMethodGenerator: RepeatedMessageMethodGenerator = JsRepeatedMessageMethodGenerator

    override val oneOfMethodAndClassGenerator: OneOfMethodAndClassGenerator = JsOneOfMethodAndClassGenerator

    override val mapMessageMethodGenerator: MapMessageMethodGenerator = JsMapMessageMethodGenerator

    override fun applyToClass(builder: TypeSpec.Builder, message: ProtoMessage) {
        val paramName = Const.Message.Constructor.JS.PARAM_IMPL

        builder.addProperty(
            PropertySpec
                .builder(paramName, message.jsType, KModifier.PUBLIC)
                .initializer(paramName)
                .build()
        )

        builder.primaryConstructor(
            FunSpec
                .constructorBuilder()
                .addParameter(paramName, message.jsType)
                .build()
        )
    }

    override fun getChildClassName(parentClass: ClassName?, childName: String): ClassName =
        getChildClassName(parentClass, childName, protoFile.pkg)

    override fun applyToEqualsFunction(
        builder: FunSpec.Builder,
        message: ProtoMessage,
        thisClassName: ClassName
    ) {
        builder.apply {
            val otherParamName = Const.Message.BasicFunctions.EqualsFunction.OTHER_PARAM

            addStatement("if (%N === this) return true", otherParamName)
            addStatement("if (%N !is %T) return false", otherParamName, thisClassName)

            message.attributes.forEach { attr ->
                when (attr.attributeType) {
                    is Scalar -> {
                        addStatement(
                            "if (%1N != %2N.%1N) return false",
                            attr.name,
                            otherParamName
                        )
                    }
                    is Repeated -> addStatement(
                        "if (%1N != %2N.%1N) return false",
                        Const.Message.Attribute.Repeated.listPropertyName(attr),
                        otherParamName
                    )
                    is MapType -> addStatement(
                        "if (%1N != %2N.%1N) return false",
                        Const.Message.Attribute.Map.propertyName(attr),
                        otherParamName
                    )
                }
            }

            addStatement("return true")
        }
    }

    override fun applyToHashCodeFunction(builder: FunSpec.Builder, message: ProtoMessage) {
        val getAttrName = { attr: ProtoMessageAttribute ->
            when (attr.attributeType) {
                is MapType -> Const.Message.Attribute.Map.propertyName(attr)
                is Repeated -> Const.Message.Attribute.Repeated.listPropertyName(attr)
                is Scalar -> attr.name
            }
        }

        builder.apply {
            if (message.attributes.isEmpty()) {
                addStatement("return 0")
                return
            }

            if (message.attributes.size == 1) {
                addStatement("return %N.hashCode()", getAttrName(message.attributes.first()))
                return
            }

            message.attributes.forEachIndexed { index, attr ->
                val attrName = getAttrName(attr)

                //Mimic the way IntelliJ generates hashCode
                if (index == 0) {
                    addStatement("var result = %N.hashCode()", attrName)
                } else {
                    addStatement("result = 31 * result + %N.hashCode()", attrName)
                }
            }

            addStatement("return result")
        }
    }
}