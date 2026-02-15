package io.github.timortel.kotlin_multiplatform_grpc_plugin.validation

import com.google.testing.junit.testparameterinjector.junit5.TestParameter
import com.google.testing.junit.testparameterinjector.junit5.TestParameterInjectorTest
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.CompilationException
import io.github.timortel.kotlin_multiplatform_grpc_plugin.FakeInputDirectory
import io.github.timortel.kotlin_multiplatform_grpc_plugin.createProtoFile
import org.junit.jupiter.api.assertThrows

class ExtensionDefinitionValidationTests : BaseValidationTest() {

    @TestParameterInjectorTest
    fun `test GIVEN an extension references an enum WHEN generating the code THEN an error is thrown`(
        @TestParameter(value = ["PROTO2", "EDITION2023", "EDITION2024"]) protoVersion: ProtoVersion
    ) {
        val declarationPrefix = getDeclarationPrefix(protoVersion)

        assertThrows<CompilationException.ExtensionInvalidReference> {
            runGenerator(
                """
                    enum A {
                        DEFAULT = 0;
                    }
                    
                    extend A {
                        $declarationPrefix string a = 1;
                    }
                """.trimIndent(),
                protoVersion = protoVersion
            )
        }
    }

    @TestParameterInjectorTest
    fun `test GIVEN duplicated extensions in the same extension WHEN generating the code THEN an error is thrown`(
        @TestParameter(value = ["PROTO2", "EDITION2023", "EDITION2024"]) protoVersion: ProtoVersion
    ) {
        val declarationPrefix = getDeclarationPrefix(protoVersion)

        assertThrows<CompilationException.DuplicateDeclaration> {
            runGenerator(
                """
                    message A {
                        extensions 1 to 5;
                    }
                    
                    extend A {
                        $declarationPrefix string a = 1;
                        $declarationPrefix string a = 2;
                    }
                """.trimIndent(),
                protoVersion = protoVersion,
            )
        }
    }

    @TestParameterInjectorTest
    fun `test GIVEN duplicated extensions in different extensions WHEN generating the code THEN an error is thrown`(
        @TestParameter(value = ["PROTO2", "EDITION2023", "EDITION2024"]) protoVersion: ProtoVersion
    ) {
        val declarationPrefix = getDeclarationPrefix(protoVersion)

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
                        $declarationPrefix string a = 1;
                    }
                    
                    extend B {
                        $declarationPrefix string a = 2;
                    }
                """.trimIndent(),
                protoVersion = protoVersion,
            )
        }
    }

    @TestParameterInjectorTest
    fun `test GIVEN a message that is not extendable and a defined extension for the message WHEN generating the code THEN an error is thrown`(
        @TestParameter(value = ["PROTO2", "EDITION2023", "EDITION2024"]) protoVersion: ProtoVersion
    ) {
        val declarationPrefix = getDeclarationPrefix(protoVersion)

        assertThrows<CompilationException.ExtensionDefinedOnNonExtendableMessage> {
            runGenerator(
                """
                    message A {
                        
                    }
                    
                    extend A {
                        $declarationPrefix string a = 1;
                    }
                """.trimIndent(),
                protoVersion = protoVersion,
            )
        }
    }

    @TestParameterInjectorTest
    fun `test GIVEN reused field numbers in extension in the extension definition WHEN generating the code THEN an error is thrown`(
        @TestParameter(value = ["PROTO2", "EDITION2023", "EDITION2024"]) protoVersion: ProtoVersion
    ) {
        val declarationPrefix = getDeclarationPrefix(protoVersion)

        assertThrows<CompilationException.FieldNumberConflict> {
            runGenerator(
                """
                    message A {
                        extensions 1 to 5;
                    }
                    
                    extend A {
                        $declarationPrefix string a = 1;
                        $declarationPrefix string b = 1;
                    }
                """.trimIndent(),
                protoVersion = protoVersion,
            )
        }
    }

    @TestParameterInjectorTest
    fun `test GIVEN reused field numbers in extension in the extension definitions across multiple files WHEN generating the code THEN an error is thrown`(
        @TestParameter(value = ["PROTO2", "EDITION2023", "EDITION2024"]) protoVersion: ProtoVersion
    ) {
        val declarationPrefix = getDeclarationPrefix(protoVersion)

        val folder = FakeInputDirectory(
            name = "dir",
            files = listOf(
                createProtoFile(
                    fileHeader = protoVersion.header,
                    """
                        message A {
                            extensions 1 to 5;
                        }
                        
                        extend A {
                            $declarationPrefix string a = 1;
                        }
                    """.trimIndent(),
                    name = "file1"
                ),
                createProtoFile(
                    fileHeader = protoVersion.header,
                    """
                        import "file1";
                        
                        extend A {
                            $declarationPrefix string b = 1;
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

    @TestParameterInjectorTest
    fun `test WHEN message has a extension definition with a minimum field number smaller than 1 THEN a compilation exception is thrown`(
        @TestParameter(value = ["PROTO2", "EDITION2023", "EDITION2024"]) protoVersion: ProtoVersion
    ) {
        val declarationPrefix = getDeclarationPrefix(protoVersion)

        assertThrows<CompilationException.ExtensionInvalidRange> {
            runGenerator(
                """
                    message A {
                        extensions 0 to 5;
                    }
                    
                    extend A {
                        $declarationPrefix string a = 1;
                    }
                """.trimIndent(),
                protoVersion = protoVersion
            )
        }
    }

    @TestParameterInjectorTest
    fun `test WHEN message has field with field number greater than max field number THEN a compilation exception is thrown`(
        @TestParameter(value = ["PROTO2", "EDITION2023", "EDITION2024"]) protoVersion: ProtoVersion
    ) {
        val declarationPrefix = getDeclarationPrefix(protoVersion)

        assertThrows<CompilationException.ExtensionInvalidRange> {
            runGenerator(
                """
                    message A {
                        extensions 1 to 536870912;
                    }
                    
                    extend A {
                        $declarationPrefix string a = 1;
                    }
                """.trimIndent(),
                protoVersion = protoVersion
            )
        }
    }

    private fun getDeclarationPrefix(protoVersion: ProtoVersion): String = when (protoVersion) {
        ProtoVersion.PROTO2 -> "optional"
        else -> ""
    }
}
