package io.github.timortel.kotlin_multiplatform_grpc_plugin.validation

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.CompilationException
import io.github.timortel.kotlin_multiplatform_grpc_plugin.FakeInputDirectory
import io.github.timortel.kotlin_multiplatform_grpc_plugin.createProtoFile
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class EnumImportValidationTest : BaseValidationTest() {

    @Test
    fun `test WHEN proto3 imports editions closed enum THEN error is thrown`() {
        assertThrows<CompilationException.IllegalClosedEnumImport> {
            runGenerator(
                listOf(
                    FakeInputDirectory(
                        name = "dir",
                        files = listOf(
                            createProtoFile(
                                fileHeader = ProtoVersion.EDITION2024.header,
                                content = """
                                enum A {
                                    option features.enum_type = CLOSED;
                                    DEFAULT = 0;
                                }
                            """.trimIndent(),
                                name = "file1.proto"
                            ),
                            createProtoFile(
                                fileHeader = ProtoVersion.PROTO3.header,
                                content = """
                                import "file1.proto";
                                
                                message B {
                                    A a = 1;
                                }
                            """.trimIndent(),
                                name = "file2.proto"
                            )
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `test WHEN proto3 imports proto2 enum THEN error is thrown`() {
        assertThrows<CompilationException.IllegalClosedEnumImport> {
            runGenerator(
                listOf(
                    FakeInputDirectory(
                        name = "dir",
                        files = listOf(
                            createProtoFile(
                                fileHeader = ProtoVersion.PROTO2.header,
                                content = """
                                enum A {
                                    DEFAULT = 0;
                                }
                            """.trimIndent(),
                                name = "file1.proto"
                            ),
                            createProtoFile(
                                fileHeader = ProtoVersion.PROTO3.header,
                                content = """
                                import "file1.proto";
                                
                                message B {
                                    A a = 1;
                                }
                            """.trimIndent(),
                                name = "file2.proto"
                            )
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `test WHEN proto3 imports open enum THEN no error is thrown`() {
        runGenerator(
            listOf(
                FakeInputDirectory(
                    name = "dir",
                    files = listOf(
                        createProtoFile(
                            fileHeader = ProtoVersion.EDITION2024.header,
                            content = """
                                enum A {
                                    option features.enum_type = OPEN;
                                    DEFAULT = 0;
                                }
                            """.trimIndent(),
                            name = "file1.proto"
                        ),
                        createProtoFile(
                            fileHeader = ProtoVersion.PROTO3.header,
                            content = """
                                import "file1.proto";
                                
                                message B {
                                    A a = 1;
                                }
                            """.trimIndent(),
                            name = "file2.proto"
                        )
                    )
                )
            )
        )
    }
}
