package io.github.timortel.kotlin_multiplatform_grpc_plugin.validation

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.CompilationException
import io.github.timortel.kotlin_multiplatform_grpc_plugin.FakeInputDirectory
import io.github.timortel.kotlin_multiplatform_grpc_plugin.createProtoFile
import io.github.timortel.kotlin_multiplatform_grpc_plugin.wellKnownTypesFolder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OptionImportTest : BaseValidationTest() {

    @Test
    fun `test GIVEN file with custom option WHEN importing it normally THEN all declarations are available`() {
        runGenerator(
            listOf(
                FakeInputDirectory(
                    name = "dir",
                    files = listOf(
                        createProtoFile(
                            fileHeader = ProtoVersion.PROTO2.header,
                            content = """
                                import "google/protobuf/descriptor.proto";
                                extend google.protobuf.MessageOptions {
                                  optional string custom_option = 51234;
                                }
                                
                                message B {}
                            """.trimIndent(),
                            name = "file1.proto"
                        ),
                        createProtoFile(
                            fileHeader = ProtoVersion.PROTO3.header,
                            content = """
                                import "file1.proto";
                                
                                message A {
                                    option (custom_option) = "some value";
                                    B b = 1;
                                }
                            """.trimIndent(),
                            name = "file2.proto"
                        ),
                        wellKnownTypesFolder
                    )
                )
            )
        )
    }

    @Test
    fun `test GIVEN file with custom option WHEN importing it as an option import and still using the declared message THEN an exception is thrown`() {
        assertThrows<CompilationException.UnresolvedReference> {
            runGenerator(
                listOf(
                    FakeInputDirectory(
                        name = "dir",
                        files = listOf(
                            createProtoFile(
                                fileHeader = ProtoVersion.PROTO2.header,
                                content = """
                                    import "google/protobuf/descriptor.proto";
                                    extend google.protobuf.MessageOptions {
                                      optional string custom_option = 51234;
                                    }
                                    
                                    message B {}
                            """.trimIndent(),
                                name = "file1.proto"
                            ),
                            createProtoFile(
                                fileHeader = ProtoVersion.EDITION2024.header,
                                content = """
                                import option "file1.proto";
                                
                                message A {
                                    option (custom_option) = "some value";
                                    B b = 1;
                                }
                            """.trimIndent(),
                                name = "file2.proto"
                            ),
                            wellKnownTypesFolder
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `test GIVEN file with custom option WHEN importing it as an option import and not using the declared message THEN no exception is thrown`() {
        runGenerator(
            listOf(
                FakeInputDirectory(
                    name = "dir",
                    files = listOf(
                        createProtoFile(
                            fileHeader = ProtoVersion.PROTO2.header,
                            content = """
                                    import "google/protobuf/descriptor.proto";
                                    extend google.protobuf.MessageOptions {
                                      optional string custom_option = 51234;
                                    }
                                    
                                    message B {}
                            """.trimIndent(),
                            name = "file1.proto"
                        ),
                        createProtoFile(
                            fileHeader = ProtoVersion.EDITION2024.header,
                            content = """
                                import option "file1.proto";
                                
                                message A {
                                    option (custom_option) = "some value";
                                }
                            """.trimIndent(),
                            name = "file2.proto"
                        ),
                        wellKnownTypesFolder
                    )
                )
            )
        )
    }
}
