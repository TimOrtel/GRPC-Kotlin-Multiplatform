package io.github.timortel.kotlin_multiplatform_grpc_plugin.validation

import io.github.timortel.kotlin_multiplatform_grpc_plugin.matchWarning
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.Warnings
import io.mockk.verify
import org.junit.jupiter.api.Test

class OptionHolderValidationTests : BaseValidationTest() {

    @Test
    fun `test WHEN unsupported options is used on message THEN a warning is printed`() {
        runGenerator(
            """
                    message TestMessage {
                        option foo = "true";
                        string field1 = 0;
                    }
                """.trimIndent()
        )

        verify(atLeast = 1) { logger.warn(matchWarning(Warnings.unsupportedOptionUsed)) }
    }

    @Test
    fun `test WHEN unsupported options is used on enum THEN a warning is printed`() {
        runGenerator(
            """
                    enum TestEnum {
                        option foo = "true";
                        field1 = 0;
                    }
                """.trimIndent()
        )

        verify(atLeast = 1) { logger.warn(matchWarning(Warnings.unsupportedOptionUsed)) }
    }

    @Test
    fun `test WHEN unsupported options is used on service THEN a warning is printed`() {
        runGenerator(
            """
                    service TestService {
                        option foo = "true";
                    }
                """.trimIndent()
        )

        verify(atLeast = 1) { logger.warn(matchWarning(Warnings.unsupportedOptionUsed)) }
    }

    @Test
    fun `test WHEN unsupported options is used on rpc THEN a warning is printed`() {
        runGenerator(
            """
                    message A {}
                    message B {}
                    service TestService {
                        rpc testRpc (A) returns (B) { option foo = "bar"; };
                    }
                """.trimIndent()
        )

        verify(atLeast = 1) { logger.warn(matchWarning(Warnings.unsupportedOptionUsed)) }
    }

    @Test
    fun `test WHEN unsupported options is used on message field THEN a warning is printed`() {
        runGenerator(
            """
                    message TestMessage {
                        string field1 = 0 [foo="bar"];
                    }
                """.trimIndent()
        )

        verify(atLeast = 1) { logger.warn(matchWarning(Warnings.unsupportedOptionUsed)) }
    }

    @Test
    fun `test WHEN unsupported options is used on message map field THEN a warning is printed`() {
        runGenerator(
            """
                    message TestMessage {
                        map<string, string> field1 = 0 [foo="bar"];
                    }
                """.trimIndent()
        )

        verify(atLeast = 1) { logger.warn(matchWarning(Warnings.unsupportedOptionUsed)) }
    }

    @Test
    fun `test WHEN unsupported options is used on message one of field THEN a warning is printed`() {
        runGenerator(
            """
                    message TestMessage {
                        oneof testOneOf {
                            string a = 1 [foo="bar"];
                        }
                    }
                """.trimIndent()
        )

        verify(atLeast = 1) { logger.warn(matchWarning(Warnings.unsupportedOptionUsed)) }
    }
}