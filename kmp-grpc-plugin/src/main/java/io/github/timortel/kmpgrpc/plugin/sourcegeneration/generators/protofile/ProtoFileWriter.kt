package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.DefaultAnnotations
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.extensions.ProtoExtensionWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.enumeration.ProtoEnumerationWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.ProtoMessageWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.service.ProtoServiceWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoBaseDeclaration
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoEnum
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile

abstract class ProtoFileWriter(val isActual: Boolean) {
    abstract val protoServiceWriter: ProtoServiceWriter
    abstract val protoMessageWriter: ProtoMessageWriter
    abstract val protoEnumWriter: ProtoEnumerationWriter

    fun generateFiles(file: ProtoFile): List<FileSpec> {
        val declarationsWithData =
            (file.messages.map { protoMessageWriter.generateProtoMessageClass(message = it) to it } +
                    file.services.map { protoServiceWriter.generateServiceStub(service = it) to it } +
                    file.enums.map { protoEnumWriter.generateProtoEnum(protoEnum = it) to it })
                .map { (type, declaration) ->
                    TopLevelDeclarationTypeData(type = type, declaration = declaration, file = file)
                }

        val hasNestedDeclarations = declarationsWithData.any { it.nestInFileClass }

        val baseFile: List<FileSpec> = if (file.extensionDefinitions.isNotEmpty() || hasNestedDeclarations) {
            val file = buildKotlinFileAndClassForProtoFile(file) {
                declarationsWithData.filter { it.nestInFileClass }.forEach { addType(it.type) }
            }

            listOf(file)
        } else emptyList()

        val topLevelDeclarationFiles = declarationsWithData
            .filterNot { it.nestInFileClass }
            .filter { it.declaration !is ProtoEnum || !isActual }
            .map {
                FileSpec
                    .builder(it.declaration.className)
                    .addAnnotation(DefaultAnnotations.SuppressDeprecation)
                    .addAnnotation(DefaultAnnotations.OptIntoKmpGrpcInternalApi)
                    .addType(it.type)
                    .build()
            }

        return baseFile + topLevelDeclarationFiles
    }

    private data class TopLevelDeclarationTypeData(
        val type: TypeSpec,
        val declaration: ProtoBaseDeclaration,
        val nestInFileClass: Boolean
    ) {
        constructor(type: TypeSpec, declaration: ProtoBaseDeclaration, file: ProtoFile) : this(
            type = type,
            declaration = declaration,
            nestInFileClass = declaration.isNested
        )
    }

    private fun buildKotlinFileAndClassForProtoFile(file: ProtoFile, builder: TypeSpec.Builder.() -> Unit): FileSpec {
        return FileSpec
            .builder(file.className)
            .addAnnotation(DefaultAnnotations.SuppressDeprecation)
            .addAnnotation(DefaultAnnotations.OptIntoKmpGrpcInternalApi)
            .addType(
                TypeSpec
                    .objectBuilder(file.className)
                    .addModifiers(file.visibility.modifier)
                    .apply {
                        addModifiers(if (isActual) KModifier.ACTUAL else KModifier.EXPECT)

                        ProtoExtensionWriter.writeExtensions(this@apply, file.extensionDefinitions, isActual)

                        builder(this@apply)
                    }
                    .build()
            )
            .build()
    }
}
