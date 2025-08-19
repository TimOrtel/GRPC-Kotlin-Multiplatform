package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.DefaultAnnotations
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.enumeration.ProtoEnumerationWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.ProtoMessageWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.service.ProtoServiceWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.Options
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile

abstract class ProtoFileWriter(val isActual: Boolean) {
    abstract val protoServiceWriter: ProtoServiceWriter
    abstract val protoMessageWriter: ProtoMessageWriter
    abstract val protoEnumWriter: ProtoEnumerationWriter

    fun generateFiles(file: ProtoFile): List<FileSpec> {
        val files = if (Options.javaMultipleFiles.get(file)) {
            val messageFiles = file.messages.map { message ->
                FileSpec
                    .builder(message.className)
                    .addAnnotation(DefaultAnnotations.SuppressDeprecation)
                    .addType(protoMessageWriter.generateProtoMessageClass(message))
                    .build()
            }

            val serviceFiles = file.services.map { service ->
                FileSpec.builder(service.className)
                    .addAnnotation(DefaultAnnotations.SuppressDeprecation)
                    .addType(protoServiceWriter.generateServiceStub(service))
                    .build()
            }

            val enumFiles = if (!isActual) {
                file.enums.map { enum ->
                    FileSpec.builder(enum.className)
                        .addAnnotation(DefaultAnnotations.SuppressDeprecation)
                        .addType(protoEnumWriter.generateProtoEnum(enum))
                        .build()
                }
            } else emptyList()

            messageFiles + serviceFiles + enumFiles
        } else {
            val file = FileSpec
                .builder(file.className)
                .addAnnotation(DefaultAnnotations.SuppressDeprecation)
                .addType(
                    TypeSpec.classBuilder(file.className)
                        .apply {
                            addModifiers(if (isActual) KModifier.ACTUAL else KModifier.EXPECT)

                            file.messages.forEach { addType(protoMessageWriter.generateProtoMessageClass(message = it)) }
                            file.services.forEach { addType(protoServiceWriter.generateServiceStub(service = it)) }

                            file.enums.forEach { addType(protoEnumWriter.generateProtoEnum(protoEnum = it)) }
                        }
                        .build()
                )
                .build()

            listOf(file)
        }

        return files
    }
}
