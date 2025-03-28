package io.github.timortel.kotlin_multiplatform_grpc_plugin.validation

import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.CompilationException
import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.Warnings
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class EnumValidationTests : BaseValidationTest() {

    @Test
    fun `test WHEN enum has two fields with the same name THEN a compilation exception is thrown`() {
        assertThrows<CompilationException.DuplicateDeclaration> {
            runGenerator(
                """
                    enum TestEnum {
                       a = 0;
                       b = 1;
                       b = 2;
                    }
                """.trimIndent()
            )
        }
    }

    @Test
    fun `test WHEN enum uses a directly reserved number THEN a compilation exception is thrown`() {
        assertThrows<CompilationException.ReservedFieldNumberUsed> {
            runGenerator(
                """
                    enum TestEnum {
                       reserved 1;
                       a = 0;
                       b = 1;
                    }
                """.trimIndent()
            )
        }
    }

    @Test
    fun `test WHEN enum uses a reserved number in range THEN a compilation exception is thrown`() {
        assertThrows<CompilationException.ReservedFieldNumberUsed> {
            runGenerator(
                """
                    enum TestEnum {
                       reserved 10 to 20;
                       a = 0;
                       b = 14;
                    }
                """.trimIndent()
            )
        }
    }

    @Test
    fun `test WHEN enum uses a reserved field name THEN a compilation exception is thrown`() {
        assertThrows<CompilationException.ReservedFieldNameUsed> {
            runGenerator(
                """
                    enum TestEnum {
                       reserved "b";
                       a = 0;
                       b = 1;
                    }
                """.trimIndent()
            )
        }
    }

    @Test
    fun `test WHEN enum does not have default field THEN a compilation exception is thrown`() {
        assertThrows<CompilationException.EnumIllegalFirstField> {
            runGenerator(
                """
                    enum TestEnum {
                       field = 1;
                    }
                """.trimIndent()
            )
        }
    }

    @Test
    fun `test WHEN enum has no field THEN compilation exception is thrown`() {
        assertThrows<CompilationException.EnumNoFields> {
            runGenerator(
                """
                    enum TestEnum {
                        
                    }
                """.trimIndent()
            )
        }
    }

    @Test
    fun `test WHEN enum has enum aliases but without the option THEN a warning is printed`() {
        runGenerator(
            """
                    enum TestEnum {
                        field1 = 0;
                        field2 = 1;
                        field3 = 1;
                    }
                """.trimIndent()
        )

        verify(atLeast = 1) { logger.warn(matchWarning(Warnings.enumAliasWithoutOption)) }
    }

    @Test
    fun `test WHEN enum has enum aliases and the option THEN no warning is printed`() {
        runGenerator(
            """
                    enum TestEnum {
                        option allow_alias = true;
                        field1 = 0;
                        field2 = 1;
                        field3 = 1;
                    }
                """.trimIndent()
        )

        verify(atLeast = 0) { logger.warn(matchWarning(Warnings.enumAliasWithoutOption)) }
    }
}