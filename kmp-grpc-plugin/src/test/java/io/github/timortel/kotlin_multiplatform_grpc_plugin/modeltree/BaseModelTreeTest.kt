package io.github.timortel.kotlin_multiplatform_grpc_plugin.modeltree

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.ProtoSourceGenerator
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoProject
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.ProtoOneOf
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.structure.ProtoPackage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.createSingleFileProtoFolder
import io.github.timortel.kotlin_multiplatform_grpc_plugin.validation.BaseValidationTest.ProtoVersion
import io.mockk.spyk
import org.junit.jupiter.api.Assertions
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class BaseModelTreeTest {

    val logger: Logger = spyk(LoggerFactory.getLogger("testlogger"))

    protected fun buildProject(content: String, protoVersion: ProtoVersion): ProtoProject {
        val folder = createSingleFileProtoFolder(protoVersion.header, content)

        return ProtoSourceGenerator.buildProtoProject(logger, listOf(folder), false)
    }

    protected fun ProtoProject.findMessage(name: String): ProtoMessage {
        return rootPackage.findMessage(name) ?: throw RuntimeException("Could not find message with name $name")
    }

    protected fun ProtoPackage.findMessage(name: String): ProtoMessage? {
        return files.firstNotNullOfOrNull { it.findMessage(name) } ?: packages.firstNotNullOfOrNull { it.findMessage(name) }
    }

    protected fun ProtoFile.findMessage(name: String): ProtoMessage? {
        return messages.firstOrNull { it.name == name } ?: messages.firstNotNullOfOrNull { it.findMessage(name) }
    }

    protected fun ProtoMessage.findMessage(name: String): ProtoMessage? {
        return messages.firstOrNull { it.name == name } ?: messages.firstNotNullOfOrNull { it.findMessage(name) }
    }

    protected fun ProtoMessage.findField(name: String): ProtoField {
        return fields.firstOrNull { it.name == name }
            ?: mapFields.firstOrNull { it.name == name }
            ?: oneOfs.firstNotNullOfOrNull { it.findField(name) }
            ?: throw RuntimeException("Could not find field with name $name")
    }

    protected fun ProtoOneOf.findField(name: String): ProtoField? {
        return fields.firstOrNull { it.name == name }
    }

    protected inline fun <reified T> Any.assertIsInstance(): T {
        return Assertions.assertInstanceOf(T::class.java, this)
    }
}
