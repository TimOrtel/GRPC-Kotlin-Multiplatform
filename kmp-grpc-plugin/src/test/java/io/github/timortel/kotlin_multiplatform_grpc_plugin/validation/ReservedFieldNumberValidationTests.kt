package io.github.timortel.kotlin_multiplatform_grpc_plugin.validation

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.CompilationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ReservedFieldNumberValidationTests : BaseValidationTest() {

    @Test
    fun `test WHEN a message uses specifically declared reserved field THEN a compilation exception is thrown`() {
        assertThrows<CompilationException.ReservedFieldNumberUsed> {
            runGenerator(
                """
                    message TestMessage {
                        reserved 2;
                        string a = 1;
                        bool b = 2;
                    }
                """.trimIndent()
            )
        }
    }

    @Test
    fun `test WHEN a message does not use specifically declared reserved field THEN no compilation exception is thrown`() {
        runGenerator(
            """
                    message TestMessage {
                        reserved 2;
                        string a = 1;
                        bool b = 3;
                    }
                """.trimIndent()
        )
    }

    @Test
    fun `test WHEN a message uses range-declared reserved field THEN a compilation exception is thrown`() {
        assertThrows<CompilationException.ReservedFieldNumberUsed> {
            runGenerator(
                """
                    message TestMessage {
                        reserved 2 to 4;
                        string a = 1;
                        string b = 3;
                    }
                """.trimIndent()
            )
        }
    }

    @Test
    fun `test WHEN a message does not use range-declared reserved field THEN no compilation exception is thrown`() {
        runGenerator(
            """
                    message TestMessage {
                        reserved 2-4;
                        string a = 1;
                        string b = 5;
                    }
                """.trimIndent()
        )
    }

    @Test
    fun `test WHEN an enum uses specifically declared reserved field THEN a compilation exception is thrown`() {
        assertThrows<CompilationException.ReservedFieldNumberUsed> {
            runGenerator(
                """
                    enum TestEnum {
                        reserved 2;
                        A = 0;
                        B = 2;
                    }
                """.trimIndent()
            )
        }
    }

    @Test
    fun `test WHEN an enum does not use specifically declared reserved field THEN no compilation exception is thrown`() {
        runGenerator(
            """
                    enum TestEnum {
                        reserved 2;
                        A = 0;
                        B = 3;
                    }
                """.trimIndent()
        )
    }

    @Test
    fun `test WHEN an enum uses range-declared reserved field THEN a compilation exception is thrown`() {
        assertThrows<CompilationException.ReservedFieldNumberUsed> {
            runGenerator(
                """
                    enum TestEnum {
                        reserved 2 to 4;
                        A = 0;
                        B = 3;
                    }
                """.trimIndent()
            )
        }
    }

    @Test
    fun `test WHEN an enum does not use range-declared reserved field THEN no compilation exception is thrown`() {
        runGenerator(
            """
                    enum TestEnum {
                        reserved 2-4;
                        A = 0;
                        B = 5;
                    }
                """.trimIndent()
        )
    }
}
