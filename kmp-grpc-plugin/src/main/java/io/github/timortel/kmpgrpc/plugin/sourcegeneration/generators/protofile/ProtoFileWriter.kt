package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.DefaultAnnotations
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.extensions.ProtoExtensionWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.enumeration.ProtoEnumerationWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.ProtoMessageWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.service.ProtoServiceWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.option.Options
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile

abstract class ProtoFileWriter(val isActual: Boolean) {
    abstract val protoServiceWriter: ProtoServiceWriter
    abstract val protoMessageWriter: ProtoMessageWriter
    abstract val protoEnumWriter: ProtoEnumerationWriter

    fun generateFiles(file: ProtoFile): List<FileSpec> {
        val files = if (Options.Basic.javaMultipleFiles.get(file)) {
            val baseFile: List<FileSpec> = if (file.extensionDefinitions.isNotEmpty()) {
                val file = buildKotlinFileAndClassForProtoFile(file) {}

                listOf(file)
            } else emptyList()

            val messageFiles = file.messages.map { message ->
                FileSpec
                    .builder(message.className)
                    .addAnnotation(DefaultAnnotations.SuppressDeprecation)
                    .addAnnotation(DefaultAnnotations.OptIntoKmpGrpcInternalApi)
                    .addType(protoMessageWriter.generateProtoMessageClass(message))
                    .build()
            }

            val serviceFiles = file.services.map { service ->
                FileSpec.builder(service.className)
                    .addAnnotation(DefaultAnnotations.SuppressDeprecation)
                    .addAnnotation(DefaultAnnotations.OptIntoKmpGrpcInternalApi)
                    .addType(protoServiceWriter.generateServiceStub(service))
                    .build()
            }

            val enumFiles = if (!isActual) {
                file.enums.map { enum ->
                    FileSpec.builder(enum.className)
                        .addAnnotation(DefaultAnnotations.SuppressDeprecation)
                        .addAnnotation(DefaultAnnotations.OptIntoKmpGrpcInternalApi)
                        .addType(protoEnumWriter.generateProtoEnum(enum))
                        .build()
                }
            } else emptyList()

            messageFiles + serviceFiles + enumFiles + baseFile
        } else {
            val file = buildKotlinFileAndClassForProtoFile(file) {
                file.messages.forEach { addType(protoMessageWriter.generateProtoMessageClass(message = it)) }
                file.services.forEach { addType(protoServiceWriter.generateServiceStub(service = it)) }

                file.enums.forEach { addType(protoEnumWriter.generateProtoEnum(protoEnum = it)) }
            }

            listOf(file)
        }

        return files
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
