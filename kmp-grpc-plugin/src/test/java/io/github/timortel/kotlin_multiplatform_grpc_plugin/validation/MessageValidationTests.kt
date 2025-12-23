package io.github.timortel.kotlin_multiplatform_grpc_plugin.validation

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.CompilationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MessageValidationTests : BaseValidationTest() {

    @Test
    fun `test WHEN message has clashing field declarations THEN a compilation exception is thrown`() {
        assertThrows<CompilationException.DuplicateDeclaration> {
            runGenerator(
                """
                    message TestMessage {
                       string a = 1;
                       bool a = 2;
                    }
                """.trimIndent()
            )
        }
    }

    @Test
    fun `test WHEN message has clashing child message declarations THEN a compilation exception is thrown`() {
        assertThrows<CompilationException.DuplicateDeclaration> {
            runGenerator(
                """
                    message TestMessage {
                       message A {}
                       message B {}
                       message B {}
                    }
                """.trimIndent()
            )
        }
    }

    @Test
    fun `test WHEN message field uses reserved field number THEN a compilation exception is thrown`() {
        assertThrows<CompilationException.ReservedFieldNumberUsed> {
            runGenerator(
                """
                    message TestMessage {
                       reserved 3;
                       string a = 1;
                       string b = 3;
                    }
                """.trimIndent()
            )
        }
    }

    @Test
    fun `test WHEN message field uses reserved field name THEN a compilation exception is thrown`() {
        assertThrows<CompilationException.ReservedFieldNameUsed> {
            runGenerator(
                """
                    message TestMessage {
                       reserved "c";
                       string a = 1;
                       string c = 3;
                    }
                """.trimIndent()
            )
        }
    }

    @Test
    fun `test WHEN message has field with field number smaller than 1 THEN a compilation exception is thrown`() {
        assertThrows<CompilationException.IllegalFieldNumber> {
            runGenerator(
                """
                    message TestMessage {
                       string a = 0;
                    }
                """.trimIndent()
            )
        }

        assertThrows<CompilationException.IllegalFieldNumber> {
            runGenerator(
                """
                    message TestMessage {
                       string a = -3;
                    }
                """.trimIndent()
            )
        }
    }

    @Test
    fun `test WHEN message has field with field number greater than max field number THEN a compilation exception is thrown`() {
        assertThrows<CompilationException.IllegalFieldNumber> {
            runGenerator(
                """
                    message TestMessage {
                       string a = 536870912;
                    }
                """.trimIndent()
            )
        }
    }
}