package io.github.timortel.kotlin_multiplatform_grpc_plugin.validation

import io.github.timortel.kotlin_multiplatform_grpc_plugin.sourcegeneration.CompilationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MessageValidationTests : BaseValidationTest() {

    @Test
    fun `test WHEN message does not have default field THEN a compilation exception is thrown`() {
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
}