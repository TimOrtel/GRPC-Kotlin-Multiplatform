package io.github.timortel.kotlin_multiplatform_grpc_plugin.generation

import com.google.testing.junit.testparameterinjector.junit5.TestParameter
import com.google.testing.junit.testparameterinjector.junit5.TestParameterInjectorTest
import io.github.timortel.kmpgrpc.plugin.NamingStrategy
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.ProtoSourceGenerator
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.SourceTarget
import io.github.timortel.kotlin_multiplatform_grpc_plugin.createSingleFileProtoFolder
import io.github.timortel.kotlin_multiplatform_grpc_plugin.validation.BaseValidationTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NestInFileClassGenerationTest : BaseGenerationTest() {

    @TestParameterInjectorTest
    fun `test GIVEN proto3 or edition2023 WHEN no option is set THEN all top level declarations are nested`(
        @TestParameter(value = ["PROTO3", "EDITION2023"]) version: BaseValidationTest.ProtoVersion
    ) {
        val fileMap = ProtoSourceGenerator.generateProtoFiles(
            logger = logger,
            protoFolders = listOf(
                createSingleFileProtoFolder(
                    fileHeader = version.header,
                    content = """
                    message A { message B {} }
                    enum C { DEFAULT = 0; }
                    service D { }
                """.trimIndent(),
                    fileName = "ProtoFile"
                )
            ),
            shouldGenerateTargetMap = targetMapAll,
            internalVisibility = false,
            namingStrategy = NamingStrategy.PROTO_LITERAL
        )

        fileMap.values.forEach { collection ->
            assertEquals(2, collection.size)
            Assertions.assertTrue { collection.any { it.name == "ProtoFile" } }
        }
    }

    @Test
    fun `test GIVEN edition2024 WHEN no option is set THEN all top level declarations have their own file`() {
        val fileMap = ProtoSourceGenerator.generateProtoFiles(
            logger = logger,
            protoFolders = listOf(
                createSingleFileProtoFolder(
                    fileHeader = BaseValidationTest.ProtoVersion.EDITION2024.header,
                    content = """
                    message A { message B {} }
                    enum C { DEFAULT = 0; }
                    service D { }
                """.trimIndent(),
                    fileName = "ProtoFile"
                )
            ),
            shouldGenerateTargetMap = targetMapAll,
            internalVisibility = false,
            namingStrategy = NamingStrategy.PROTO_LITERAL
        )

        fileMap.entries.forEach { (target, collection) ->
            when (target) {
                SourceTarget.Common -> {
                    assertEquals(4, collection.size)
                    Assertions.assertTrue { collection.any { it.name == "A" } }
                    Assertions.assertTrue { collection.any { it.name == "C" } }
                    Assertions.assertTrue { collection.any { it.name == "DStub" } }
                }
                is SourceTarget.Actual -> {
                    assertEquals(3, collection.size)
                    Assertions.assertTrue { collection.any { it.name == "A" } }
                    Assertions.assertTrue { collection.none { it.name == "C" } }
                    Assertions.assertTrue { collection.any { it.name == "DStub" } }
                }
            }
        }
    }

    @TestParameterInjectorTest
    fun `test GIVEN proto3 or edition2023 WHEN multiple files option is set THEN all top level declarations have their own file`(
        @TestParameter(value = ["PROTO3", "EDITION2023"]) version: BaseValidationTest.ProtoVersion
    ) {
        val fileMap = ProtoSourceGenerator.generateProtoFiles(
            logger = logger,
            protoFolders = listOf(
                createSingleFileProtoFolder(
                    fileHeader = version.header,
                    content = """
                        option java_multiple_files = true;
                        message A { message B {} }
                        enum C { DEFAULT = 0; }
                        service D { }
                    """.trimIndent()
                )
            ),
            shouldGenerateTargetMap = targetMapAll,
            internalVisibility = false,
            namingStrategy = NamingStrategy.PROTO_LITERAL
        )

        fileMap.entries.forEach { (target, collection) ->
            when (target) {
                SourceTarget.Common -> {
                    assertEquals(4, collection.size)
                    Assertions.assertTrue { collection.any { it.name == "A" } }
                    Assertions.assertTrue { collection.any { it.name == "C" } }
                    Assertions.assertTrue { collection.any { it.name == "DStub" } }
                }
                is SourceTarget.Actual -> {
                    assertEquals(3, collection.size)
                    Assertions.assertTrue { collection.any { it.name == "A" } }
                    Assertions.assertTrue { collection.none { it.name == "C" } }
                    Assertions.assertTrue { collection.any { it.name == "DStub" } }
                }
            }
        }
    }

    @Test
    fun `test GIVEN edition2024 WHEN legacy feature is set THEN fallback on option happens`() {
        val fileMap = ProtoSourceGenerator.generateProtoFiles(
            logger = logger,
            protoFolders = listOf(
                createSingleFileProtoFolder(
                    fileHeader = BaseValidationTest.ProtoVersion.EDITION2024.header,
                    content = """
                        import "google/protobuf/java_features.proto";
                        
                        option java_multiple_files = false;
                        
                        message A { 
                            option features.(pb.java).nest_in_file_class = LEGACY;
                            message B {} 
                        }
                        enum C {
                            option features.(pb.java).nest_in_file_class = LEGACY;
                            DEFAULT = 0;
                        }
                        service D {
                            option features.(pb.java).nest_in_file_class = LEGACY;
                        }
                    """.trimIndent(),
                    fileName = "ProtoFile"
                )
            ),
            shouldGenerateTargetMap = targetMapAll,
            internalVisibility = false,
            namingStrategy = NamingStrategy.PROTO_LITERAL
        )

        fileMap.values.forEach { collection ->
            assertEquals(2, collection.size)
        }
    }

    @Test
    fun `test GIVEN edition2024 WHEN a mix of features is set THEN the correct declarations are in their own file`() {
        val fileMap = ProtoSourceGenerator.generateProtoFiles(
            logger = logger,
            protoFolders = listOf(
                createSingleFileProtoFolder(
                    fileHeader = BaseValidationTest.ProtoVersion.EDITION2024.header,
                    content = """
                        import "google/protobuf/java_features.proto";
                        
                        message A { option features.(pb.java).nest_in_file_class = YES; }
                        enum C { 
                            option features.(pb.java).nest_in_file_class = NO;
                            DEFAULT = 0; 
                        }
                        service D { option features.(pb.java).nest_in_file_class = YES; }
                    """.trimIndent(),
                    fileName = "ProtoFile"
                )
            ),
            shouldGenerateTargetMap = targetMapAll,
            internalVisibility = false,
            namingStrategy = NamingStrategy.PROTO_LITERAL
        )

        fileMap.entries.forEach { (target, collection) ->
            when (target) {
                SourceTarget.Common -> {
                    assertEquals(3, collection.size)
                    Assertions.assertTrue { collection.any { it.name == "ProtoFile" } }
                    Assertions.assertTrue { collection.any { it.name == "C" } }
                }
                is SourceTarget.Actual -> {
                    assertEquals(2, collection.size)
                    Assertions.assertTrue { collection.any { it.name == "ProtoFile" } }
                    Assertions.assertTrue { collection.none { it.name == "C" } }
                }
            }
        }
    }
}
