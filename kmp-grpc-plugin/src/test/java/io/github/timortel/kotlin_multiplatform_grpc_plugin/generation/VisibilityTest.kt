package io.github.timortel.kotlin_multiplatform_grpc_plugin.generation

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.ProtoSourceGenerator
import io.github.timortel.kotlin_multiplatform_grpc_plugin.createSingleFileProtoFolderFromResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class VisibilityTest : BaseGenerationTest() {

    @Test
    fun `test GIVEN the default visibility is public WHEN generating the code THEN all generated classes are public`() {
        testDefaultCodeGeneration(internalVisibility = false, expectedModifier = KModifier.PUBLIC)
    }

    @Test
    fun `test GIVEN the default visibility is internal WHEN generating the code THEN all generated classes are internal`() {
        testDefaultCodeGeneration(internalVisibility = true, expectedModifier = KModifier.INTERNAL)
    }

    private fun testDefaultCodeGeneration(internalVisibility: Boolean, expectedModifier: KModifier) {
        val fileMap = ProtoSourceGenerator.generateProtoFiles(
            logger = logger,
            protoFolders = listOf(
                createSingleFileProtoFolderFromResource(
                    classLoader = javaClass.classLoader,
                    fileName = "declaration-test-basic.proto"
                )
            ),
            shouldGenerateTargetMap = targetMapAll,
            internalVisibility = internalVisibility
        )

        fileMap.values.forEach { files ->
            files.forEach { file ->
                file.members.forEach { member ->
                    val message = "Expected modifiers to contain $expectedModifier, but not found. Member=$member"

                    when (member) {
                        is TypeSpec -> {
                            Assertions.assertTrue(member.modifiers.any { it == expectedModifier }, message)
                        }

                        is FunSpec -> {
                            Assertions.assertTrue(member.modifiers.any { it == expectedModifier }, message)
                        }

                        else -> throw IllegalStateException("Unknown member $member")
                    }
                }
            }
        }
    }
}
