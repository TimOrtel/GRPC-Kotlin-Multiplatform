package io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.enumeration.ProtoEnumerationWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.protofile.message.ProtoMessageWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.generators.service.ProtoServiceWriter
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.Options
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import java.io.File

abstract class ProtoFileWriter(val isActual: Boolean) {
    abstract val protoServiceWriter: ProtoServiceWriter
    abstract val protoMessageWriter: ProtoMessageWriter
    abstract val protoEnumWriter: ProtoEnumerationWriter

    open fun writeFiles(file: ProtoFile, outputDir: File) {
        if (Options.javaMultipleFiles.get(file)) {
            file.messages.forEach { message ->
                FileSpec
                    .builder(message.className)
                    .addType(protoMessageWriter.generateProtoMessageClass(message))
                    .build()
                    .writeTo(outputDir)
            }

            file.services.forEach { service ->
                FileSpec.builder(service.className)
                    .addType(protoServiceWriter.generateServiceStub(service))
                    .build()
                    .writeTo(outputDir)
            }

            if (!isActual) {
                file.enums.forEach { enum ->
                    FileSpec.builder(enum.className)
                        .addType(protoEnumWriter.generateProtoEnum(enum))
                        .build()
                        .writeTo(outputDir)
                }
            }
        } else {
            FileSpec
                .builder(file.className)
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
                .writeTo(outputDir)
        }
    }
}
