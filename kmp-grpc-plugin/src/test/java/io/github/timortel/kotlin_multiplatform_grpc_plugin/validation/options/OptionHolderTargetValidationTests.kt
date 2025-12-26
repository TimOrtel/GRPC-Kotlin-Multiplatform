package io.github.timortel.kotlin_multiplatform_grpc_plugin.validation.options

import com.google.testing.junit.testparameterinjector.junit5.TestParameter
import com.google.testing.junit.testparameterinjector.junit5.TestParameterInjectorTest
import com.google.testing.junit.testparameterinjector.junit5.TestParameterValuesProvider
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.CompilationException
import io.github.timortel.kotlin_multiplatform_grpc_plugin.validation.BaseValidationTest
import io.mockk.verify
import org.junit.jupiter.api.assertThrows
import kotlin.collections.plus

class OptionHolderTargetValidationTests : BaseValidationTest() {

    data class OptionApplicationScenario(val content: String, val version: ProtoVersion)

    class ValidOptionApplicationScenarioProvider : TestParameterValuesProvider() {
        override fun provideValues(context: Context?): List<*> {
            val allVersionScenarios = listOf(
                """
                    option java_package = foo.bar;
                """.trimIndent()
                )

            val proto3Scenarios = listOf(
                """
                    message A {
                        repeated int32 a = 1 [packed = true];
                    }
                """.trimIndent()
            )

            val featureScenarios = listOf(
                """
                    option features.field_presence = EXPLICIT;
                """.trimIndent(),
                """
                    message A {
                        repeated int32 a = 1 [features.field_presence = EXPLICIT];
                    }
                """.trimIndent(),
                """
                    message A {
                        repeated int32 a = 1 [features.repeated_field_encoding = PACKED];
                    }
                """.trimIndent()
            )

            return allVersionScenarios.flatMap { ProtoVersion.entries.map { version -> OptionApplicationScenario(it, version) } } +
                    proto3Scenarios.map { content -> OptionApplicationScenario(content, ProtoVersion.PROTO3) } +
                    featureScenarios.flatMap { listOf(ProtoVersion.EDITION2023, ProtoVersion.EDITION2024).map { version -> OptionApplicationScenario(it, version) } }
        }
    }

    @TestParameterInjectorTest
    fun `test WHEN target option is applied to correct target THEN no warning is printed`(
        @TestParameter(valuesProvider = ValidOptionApplicationScenarioProvider::class) scenario: OptionApplicationScenario
    ) {
        runGenerator(
            content = scenario.content,
            protoVersion = scenario.version
        )

        verify(exactly = 0) { logger.warn(any()) }
    }

    class InvalidOptionApplicationScenarioProvider : TestParameterValuesProvider() {
        override fun provideValues(context: Context?): List<*> {
            val allVersionScenarios = listOf(
                """
                    message A {
                        option java_package = foo.bar;
                    }
                """.trimIndent()
            )

            val proto3Scenarios = listOf(
                """
                    message A {
                        int32 a = 1 [packed = true];
                    }
                """.trimIndent()
            )

            val featureScenarios = listOf(
                """
                    enum A {
                        option features.field_presence = EXPLICIT;
                        DEFAULT = 0;
                    }
                """.trimIndent(),
                """
                    message A {
                        int32 a = 1 [features.repeated_field_encoding = PACKED];
                    }
                """.trimIndent(),
                """
                    message A {
                        repeated string a = 1 [features.repeated_field_encoding = PACKED];
                    }
                """.trimIndent(),
                """
                    message A {
                        int32 a = 1 [features.repeated_field_encoding = PACKED];
                    }
                """.trimIndent()
            )

            return allVersionScenarios.flatMap { ProtoVersion.entries.map { version -> OptionApplicationScenario(it, version) } } +
                    proto3Scenarios.map { content -> OptionApplicationScenario(content, ProtoVersion.PROTO3) } +
                    featureScenarios.flatMap { listOf(ProtoVersion.EDITION2023, ProtoVersion.EDITION2024).map { version -> OptionApplicationScenario(it, version) } }
        }
    }

    @TestParameterInjectorTest
    fun `test WHEN file target option is applied to message level THEN exception is thrown`(
        @TestParameter(valuesProvider = InvalidOptionApplicationScenarioProvider::class) scenario: OptionApplicationScenario
    ) {
        assertThrows<CompilationException.OptionInvalidTarget> {
            runGenerator(
                content = scenario.content,
                protoVersion = scenario.version
            )
        }
    }
}
