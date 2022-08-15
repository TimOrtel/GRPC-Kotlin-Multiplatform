package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.proto_file

import com.squareup.kotlinpoet.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.IOSMapMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.MapMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof.IOSOneOfMethodAndClassGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof.OneOfMethodAndClassGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.repeated.IOSRepeatedMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.repeated.RepeatedMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.scalar.IOSScalarMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.scalar.ScalarMessageMethodGenerator

class IOSProtoFileWriter(private val protoFile: ProtoFile) : ProtoFileWriter(protoFile, isActual = true),
    DefaultChildClassName {

    override val scalarMessageMethodGenerator: ScalarMessageMethodGenerator
        get() = IOSScalarMessageMethodGenerator
    override val repeatedMessageMethodGenerator: RepeatedMessageMethodGenerator
        get() = IOSRepeatedMessageMethodGenerator
    override val oneOfMethodAndClassGenerator: OneOfMethodAndClassGenerator
        get() = IOSOneOfMethodAndClassGenerator
    override val mapMessageMethodGenerator: MapMessageMethodGenerator
        get() = IOSMapMessageMethodGenerator

    override fun applyToClass(builder: TypeSpec.Builder, message: ProtoMessage) {
        builder.apply {
            primaryConstructor(
                FunSpec
                    .constructorBuilder()
                    .apply {
                        message.attributes.forEach { attr ->
                            if (attr.types.isNullable) {
                                addParameter(attr.name, attr.commonType.copy(nullable = true))
                            } else {
                                addParameter(attr.name, attr.commonType)
                            }
                        }
                    }
                    .build()
            )

            addSuperinterface(kmMessage)

            addProperty(
                PropertySpec
                    .builder("requiredSize", U_LONG, KModifier.OVERRIDE)
                    .initializer("0u")
                    .build()
            )

            addFunction(
                FunSpec
                    .builder("serialize")
                    .addParameter("stream", GPBCodedOutputStream)
                    .build()
            )

            addFunction(
                FunSpec
                    .builder("serialize")
                    .returns(NSData)
                    .addStatement("val data = %T().apply { setLength(requiredSize) }", NSMutableData)
                    .addStatement("val stream = %T(data)", GPBCodedOutputStream)
                    .addStatement("serialize(stream)")
                    .addStatement("return data")
                    .build()
            )
        }
    }

    override fun applyToEqualsFunction(builder: FunSpec.Builder, message: ProtoMessage, thisClassName: ClassName) {

    }

    override fun applyToHashCodeFunction(builder: FunSpec.Builder, message: ProtoMessage) {

    }

    override fun getChildClassName(parentClass: ClassName?, childName: String): ClassName =
        getChildClassName(parentClass, childName, protoFile.pkg)
}