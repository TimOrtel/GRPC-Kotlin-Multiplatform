package io.github.timortel.kotlin_multiplatform_grpc_plugin.validation

import com.google.testing.junit.testparameterinjector.junit5.TestParameter
import com.google.testing.junit.testparameterinjector.junit5.TestParameterInjectorTest
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.Warnings
import io.github.timortel.kotlin_multiplatform_grpc_plugin.matchWarning
import io.mockk.verify
import org.junit.jupiter.api.Test

class OptionHolderValidationTests : BaseValidationTest() {

    @TestParameterInjectorTest
    fun `test WHEN unsupported options is used on message THEN a warning is printed`(
        @TestParameter protoVersion: ProtoVersion
    ) {
        runGenerator(
            """
                    message TestMessage {
                        option foo = "true";
                        string field1 = 1;
                    }
                """.trimIndent(),
            protoVersion
        )

        verify(atLeast = 1) { logger.warn(matchWarning(Warnings.unsupportedOptionUsed)) }
    }

    @TestParameterInjectorTest
    fun `test WHEN unsupported options is used on enum THEN a warning is printed`(
        @TestParameter protoVersion: ProtoVersion
    ) {
        runGenerator(
            """
                    enum TestEnum {
                        option foo = "true";
                        field1 = 0;
                    }
                """.trimIndent(),
            protoVersion
        )

        verify(atLeast = 1) { logger.warn(matchWarning(Warnings.unsupportedOptionUsed)) }
    }

    @TestParameterInjectorTest
    fun `test WHEN unsupported options is used on service THEN a warning is printed`(
        @TestParameter protoVersion: ProtoVersion
    ) {
        runGenerator(
            """
                    service TestService {
                        option foo = "true";
                    }
                """.trimIndent(),
            protoVersion
        )

        verify(atLeast = 1) { logger.warn(matchWarning(Warnings.unsupportedOptionUsed)) }
    }

    @TestParameterInjectorTest
    fun `test WHEN unsupported options is used on rpc THEN a warning is printed`(
        @TestParameter protoVersion: ProtoVersion
    ) {
        runGenerator(
            """
                    message A {}
                    message B {}
                    service TestService {
                        rpc testRpc (A) returns (B) { option foo = "bar"; };
                    }
                """.trimIndent(),
            protoVersion
        )

        verify(atLeast = 1) { logger.warn(matchWarning(Warnings.unsupportedOptionUsed)) }
    }

    @TestParameterInjectorTest
    fun `test WHEN unsupported options is used on message field THEN a warning is printed`(
        @TestParameter protoVersion: ProtoVersion
    ) {
        runGenerator(
            """
                    message TestMessage {
                        string field1 = 1 [foo="bar"];
                    }
                """.trimIndent(),
            protoVersion
        )

        verify(atLeast = 1) { logger.warn(matchWarning(Warnings.unsupportedOptionUsed)) }
    }

    @TestParameterInjectorTest
    fun `test WHEN unsupported options is used on message map field THEN a warning is printed`(
        @TestParameter protoVersion: ProtoVersion
    ) {
        runGenerator(
            """
                    message TestMessage {
                        map<string, string> field1 = 1 [foo="bar"];
                    }
                """.trimIndent(),
            protoVersion
        )

        verify(atLeast = 1) { logger.warn(matchWarning(Warnings.unsupportedOptionUsed)) }
    }

    @TestParameterInjectorTest
    fun `test WHEN unsupported options is used on message one of field THEN a warning is printed`(
        @TestParameter protoVersion: ProtoVersion
    ) {
        runGenerator(
            """
                    message TestMessage {
                        oneof testOneOf {
                            string a = 1 [foo="bar"];
                        }
                    }
                """.trimIndent(),
            protoVersion
        )

        verify(atLeast = 1) { logger.warn(matchWarning(Warnings.unsupportedOptionUsed)) }
    }

    @TestParameterInjectorTest
    fun `test WHEN packed option is used on a non-repeated field THEN a warning is printed`(
        @TestParameter protoVersion: ProtoVersion
    ) {
        runGenerator(
            """
                    message TestMessage {
                        bool a = 1 [packed = false];
                    }
                """.trimIndent(),
            protoVersion
        )

        verify(atLeast = 1) { logger.warn(matchWarning(Warnings.unsupportedOptionUsed)) }
    }

    @TestParameterInjectorTest
    fun `test WHEN packed option is used on a repeated field that has a non-packable type THEN a warning is printed`(
        @TestParameter protoVersion: ProtoVersion
    ) {
        runGenerator(
            """
                    message TestMessage {
                        repeated string a = 1 [packed = false];
                    }
                """.trimIndent(),
            protoVersion
        )

        verify(atLeast = 1) { logger.warn(matchWarning(Warnings.unsupportedOptionUsed)) }
    }

    @TestParameterInjectorTest
    fun `test WHEN packed option is used on a repeated field that has a packable type THEN no warning is printed`(
        @TestParameter protoVersion: ProtoVersion
    ) {
        runGenerator(
            """
                    message TestMessage {
                        repeated int32 a = 1 [packed = false];
                    }
                """.trimIndent(),
            protoVersion
        )

        verify(exactly = 0) { logger.warn(matchWarning(Warnings.unsupportedOptionUsed)) }
    }

    @Test
    fun `test WHEN feature option is used on proto3 THEN a warning is printed`() {
        runGenerator(
            """
                option features.field_presence = IMPLICIT;
            """.trimIndent(),
            protoVersion = ProtoVersion.PROTO3
        )

        verify(exactly = 1) { logger.warn(matchWarning(Warnings.unsupportedOptionUsed)) }
    }

    @Test
    fun `test WHEN feature option is used on edition2023 THEN no warning is printed`() {
        runGenerator(
            """
                option features.field_presence = IMPLICIT;
            """.trimIndent(),
            protoVersion = ProtoVersion.EDITION2023
        )

        verify(exactly = 0) { logger.warn(matchWarning(Warnings.unsupportedOptionUsed)) }
    }
}
