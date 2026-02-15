package io.github.timortel.kotlin_multiplatform_grpc_plugin.generation

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import io.github.timortel.kmpgrpc.plugin.NamingStrategy
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.ProtoSourceGenerator
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SourceTarget
import io.github.timortel.kotlin_multiplatform_grpc_plugin.createSingleFileProtoFolderFromResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf

class DeprecatedOptionGenerationTest : BaseGenerationTest() {

    @Test
    fun `test generates deprecated annotation correctly`() {
        val fileMap = ProtoSourceGenerator.generateProtoFiles(
            logger = logger,
            protoFolders = listOf(
                createSingleFileProtoFolderFromResource(
                    classLoader = javaClass.classLoader,
                    fileName = "deprecated-test.proto"
                )
            ),
            shouldGenerateTargetMap = targetMapAll,
            internalVisibility = false,
            namingStrategy = NamingStrategy.PROTO_LITERAL
        )

        val deprecatedProperties = listOf(
            "deprecatedField",
            "deprecatedList",
            "deprecatedMap"
        )

        val nonDeprecatedProperties = listOf(
            "nonDeprecatedField",
            "nonDeprecatedList",
            "nonDeprecatedMap"
        )

        fileMap.values.forEach { targetFiles ->
            val msgWithDeprecatedFieldsFile = targetFiles.first { it.name == "MessageWithDeprecatedFields" }
            val messageClazz = msgWithDeprecatedFieldsFile.members.first()
            assertInstanceOf<TypeSpec>(messageClazz)

            deprecatedProperties.forEach { depProp ->
                val prop = messageClazz.propertySpecs.first { it.name.contains(depProp) }
                prop.annotations.assertDeprecated()
            }

            nonDeprecatedProperties.forEach { prop ->
                val prop = messageClazz.propertySpecs.first { it.name.contains(prop) }
                prop.annotations.assertNotDeprecated()
            }

            val oneOf = messageClazz.typeSpecs.first { it.name?.contains("DeprecatedOneOf") ?: false }
            val nonDeprecatedOneOfOption =
                oneOf.typeSpecs.first { it.name == "NonDeprecatedOneOfOption" }
            val deprecatedOneOfOption = oneOf.typeSpecs.first { it.name == "DeprecatedOneOfOption" }

            nonDeprecatedOneOfOption.annotations.assertNotDeprecated()
            deprecatedOneOfOption.annotations.assertDeprecated()
        }

        val enumFile = fileMap[SourceTarget.Common].orEmpty().first { it.name == "DeprecatedEnum" }
        val enumClazz = enumFile.members.first()
        assertInstanceOf<TypeSpec>(enumClazz)

        enumClazz.typeSpecs.first { it.name == "DEFAULT" }.annotations.assertNotDeprecated()
        enumClazz.typeSpecs.first { it.name == "DEPRECATED_OPTION" }.annotations.assertNotDeprecated()
    }

    private fun List<AnnotationSpec>.assertDeprecated() {
        Assertions.assertTrue(
            this.any { it.typeName == Deprecated::class.asTypeName() },
            "Expected annotations $this to have a deprecated annotation"
        )
    }

    private fun List<AnnotationSpec>.assertNotDeprecated() {
        Assertions.assertFalse(
            this.any { it.typeName == Deprecated::class.asTypeName() },
            "Expected annotations $this to not have a deprecated annotation"
        )
    }
}
