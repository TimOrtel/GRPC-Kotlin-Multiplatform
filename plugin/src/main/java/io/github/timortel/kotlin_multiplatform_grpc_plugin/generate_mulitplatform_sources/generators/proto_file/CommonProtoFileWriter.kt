package io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.proto_file

import com.squareup.kotlinpoet.*
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.Const
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.CommonMapMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.map.MapMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof.CommonOneOfMethodAndClassGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.oneof.OneOfMethodAndClassGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.repeated.CommonRepeatedMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.repeated.RepeatedMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.scalar.CommonScalarMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.generators.scalar.ScalarMessageMethodGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.generate_mulitplatform_sources.content.ProtoMessage
import java.io.File

/**
 * File writer that writes Kotlin Multiplatform Proto Files. These are platform independent.
 */
class CommonProtoFileWriter(private val protoFile: ProtoFile) : ProtoFileWriter(protoFile, false),
    DefaultChildClassName {

    override val scalarMessageMethodGenerator: ScalarMessageMethodGenerator = CommonScalarMessageMethodGenerator

    override val repeatedMessageMethodGenerator: RepeatedMessageMethodGenerator = CommonRepeatedMessageMethodGenerator

    override val oneOfMethodAndClassGenerator: OneOfMethodAndClassGenerator = CommonOneOfMethodAndClassGenerator

    override val mapMessageMethodGenerator: MapMessageMethodGenerator = CommonMapMessageMethodGenerator

    override fun writeFile(outputDir: File) {
        super.writeFile(outputDir)

        //Top level enums are only written in the common module
        protoFile.enums.forEach { topLevelEnum ->
            FileSpec
                .builder(protoFile.pkg, Const.Enum.commonEnumName(topLevelEnum))
                .apply {
                    addProtoEnum(this::addType, EnumType.TOP_LEVEL, topLevelEnum) { childName ->
                        ClassName(
                            protoFile.pkg,
                            childName
                        )
                    }
                }
                .build()
                .writeTo(outputDir)
        }
    }

    override fun applyToClass(builder: TypeSpec.Builder, message: ProtoMessage) = Unit

    override fun getChildClassName(parentClass: ClassName?, childName: String): ClassName =
        getChildClassName(parentClass, childName, protoFile.pkg)
}