package io.github.timortel.kotlin_multiplatform_grpc_plugin.validation

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.CompilationException
import io.github.timortel.kotlin_multiplatform_grpc_plugin.FakeInputDirectory
import io.github.timortel.kotlin_multiplatform_grpc_plugin.createProtoFile
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ExtensionDefinitionValidationTests : BaseValidationTest() {

    @Test
    fun `test GIVEN an extension references an enum WHEN generating the code THEN an error is thrown`() {
        assertThrows<CompilationException.ExtensionInvalidReference> {
            runGenerator(
                """
                    enum A {
                        DEFAULT = 0;
                    }
                    
                    extend A {
                        string a = 1;
                    }
                """.trimIndent(),
                protoVersion = ProtoVersion.EDITION2023,
            )
        }
    }

    @Test
    fun `test GIVEN duplicated extensions in the same extension WHEN generating the code THEN an error is thrown`() {
        assertThrows<CompilationException.DuplicateDeclaration> {
            runGenerator(
                """
                    message A {
                        extensions 1 to 5;
                    }
                    
                    extend A {
                        string a = 1;
                        string a = 2;
                    }
                """.trimIndent(),
                protoVersion = ProtoVersion.EDITION2023,
            )
        }
    }

    @Test
    fun `test GIVEN duplicated extensions in different extensions WHEN generating the code THEN an error is thrown`() {
        assertThrows<CompilationException.DuplicateDeclaration> {
            runGenerator(
                """
                    message A {
                        extensions 1 to 5;
                    }
                    
                    message B {
                        extensions 1 to 5;
                    }
                    
                    extend A {
                        string a = 1;
                    }
                    
                    extend B {
                        string a = 2;
                    }
                """.trimIndent(),
                protoVersion = ProtoVersion.EDITION2023,
            )
        }
    }

    @Test
    fun `test GIVEN a message that is not extendable and a defined extension for the message WHEN generating the code THEN an error is thrown`() {
        assertThrows<CompilationException.ExtensionDefinedOnNonExtendableMessage> {
            runGenerator(
                """
                    message A {
                        
                    }
                    
                    extend A {
                        string a = 1;
                    }
                """.trimIndent(),
                protoVersion = ProtoVersion.EDITION2023,
            )
        }
    }

    @Test
    fun `test GIVEN reused field numbers in extension in the extension definition WHEN generating the code THEN an error is thrown`() {
        assertThrows<CompilationException.FieldNumberConflict> {
            runGenerator(
                """
                    message A {
                        extensions 1 to 5;
                    }
                    
                    extend A {
                        string a = 1;
                        string b = 1;
                    }
                """.trimIndent(),
                protoVersion = ProtoVersion.EDITION2023,
            )
        }
    }

    @Test
    fun `test GIVEN reused field numbers in extension in the extension definitions across multiple files WHEN generating the code THEN an error is thrown`() {
        val folder = FakeInputDirectory(
            name = "dir",
            files = listOf(
                createProtoFile(
                    fileHeader = ProtoVersion.EDITION2023.header,
                    """
                        message A {
                            extensions 1 to 5;
                        }
                        
                        extend A {
                            string a = 1;
                        }
                    """.trimIndent(),
                    name = "file1"
                ),
                createProtoFile(
                    fileHeader = ProtoVersion.EDITION2023.header,
                    """
                        import "file1";
                        
                        extend A {
                            string b = 1;
                        }
                    """.trimIndent(),
                    name = "file2"
                )
            )
        )

        assertThrows<CompilationException.FieldNumberConflict> {
            runGenerator(listOf(folder))
        }
    }
}
