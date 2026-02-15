package io.github.timortel.kotlin_multiplatform_grpc_plugin.modeltree

import io.github.timortel.kmpgrpc.plugin.NamingStrategy
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.ProtoSourceGenerator
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoProject
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoEnum
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoMessage
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.enumeration.ProtoEnumField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.ProtoOneOf
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMessageField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoOneOfField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.service.ProtoRpc
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.service.ProtoService
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.structure.ProtoPackage
import io.github.timortel.kotlin_multiplatform_grpc_plugin.createSingleFileProtoFolder
import io.github.timortel.kotlin_multiplatform_grpc_plugin.validation.BaseValidationTest.ProtoVersion
import io.mockk.spyk
import org.junit.jupiter.api.Assertions
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class BaseModelTreeTest {

    val logger: Logger = spyk(LoggerFactory.getLogger("testlogger"))

    protected fun buildProject(
        content: String,
        protoVersion: ProtoVersion,
        namingStrategy: NamingStrategy = NamingStrategy.PROTO_LITERAL
    ): ProtoProject {
        val folder = createSingleFileProtoFolder(protoVersion.header, content)

        return ProtoSourceGenerator.buildProtoProject(
            logger = logger,
            protoFolders = listOf(folder),
            internalVisibility = false,
            namingStrategy = namingStrategy
        )
    }

    protected fun ProtoProject.findMessage(name: String): ProtoMessage {
        return rootPackage.findMessage(name) ?: throw RuntimeException("Could not find message with name $name")
    }

    protected fun ProtoPackage.findMessage(name: String): ProtoMessage? {
        return find { it.findMessage(name) }
    }

    protected fun ProtoProject.findEnum(name: String): ProtoEnum {
        return rootPackage.find { file -> file.findEnum(name) }
            ?: throw RuntimeException("Could not find enum with name $name")
    }

    protected fun ProtoFile.findMessage(name: String): ProtoMessage? {
        return messages.firstOrNull { it.name == name } ?: messages.firstNotNullOfOrNull { it.findMessage(name) }
    }

    protected fun ProtoFile.findEnum(name: String): ProtoEnum? {
        return enums.firstOrNull { it.name == name } ?: messages.firstNotNullOfOrNull { it.findEnum(name) }
    }

    protected fun ProtoMessage.findMessage(name: String): ProtoMessage? {
        return messages.firstOrNull { it.name == name } ?: messages.firstNotNullOfOrNull { it.findMessage(name) }
    }

    protected fun ProtoMessage.findEnum(name: String): ProtoEnum? {
        return enums.firstOrNull { it.name == name } ?: messages.firstNotNullOfOrNull { it.findEnum(name) }
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

    protected fun ProtoMessage.findOneOf(name: String): ProtoOneOf {
        return oneOfs.firstOrNull { it.name == name } ?: throw RuntimeException("Could not find one of with name $name")
    }

    protected fun ProtoProject.findExtensionField(name: String): ProtoMessageField {
        return rootPackage.find { file ->
            file
                .findExtensionDefinitions()
                .flatMap { it.fields }
                .firstOrNull { it.name == name }
        } ?: throw RuntimeException("Could not find extension field with name $name")
    }

    protected fun ProtoProject.findService(name: String): ProtoService {
        return rootPackage.find { file ->
            file.services.firstOrNull { it.name == name }
        } ?: throw RuntimeException("Could not find service with name $name")
    }

    protected fun ProtoService.findRpc(name: String): ProtoRpc {
        return rpcs.firstOrNull { it.name == name } ?: throw RuntimeException("Could not find rpc with name $name")
    }

    protected fun ProtoOneOf.findCase(name: String): ProtoOneOfField {
        return fields.firstOrNull { it.name == name } ?: throw RuntimeException("Could not find case with name $name")
    }

    protected fun ProtoEnum.findEntry(name: String): ProtoEnumField {
        return fields.firstOrNull { it.name == name } ?: throw RuntimeException("Could not find enum entry with name $name")
    }

    private fun <T> ProtoPackage.find(block: (ProtoFile) -> T?): T? {
        return files.firstNotNullOfOrNull { block(it) } ?: packages.firstNotNullOfOrNull {
            it.find(block)
        }
    }

    protected inline fun <reified T> Any.assertIsInstance(): T {
        return Assertions.assertInstanceOf(T::class.java, this)
    }
}
