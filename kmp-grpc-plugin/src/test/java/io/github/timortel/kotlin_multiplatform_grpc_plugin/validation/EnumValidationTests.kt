package io.github.timortel.kotlin_multiplatform_grpc_plugin.validation

import com.google.testing.junit.testparameterinjector.junit5.TestParameter
import com.google.testing.junit.testparameterinjector.junit5.TestParameterInjectorTest
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.CompilationException
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.Warnings
import io.github.timortel.kotlin_multiplatform_grpc_plugin.matchWarning
import io.mockk.verify
import org.junit.jupiter.api.assertThrows

class EnumValidationTests : BaseValidationTest() {

    @TestParameterInjectorTest
    fun `test WHEN enum has two fields with the same name THEN a compilation exception is thrown`(
        @TestParameter protoVersion: ProtoVersion
    ) {
        assertThrows<CompilationException.DuplicateDeclaration> {
            runGenerator(
                """
                    enum TestEnum {
                       a = 0;
                       b = 1;
                       b = 2;
                    }
                """.trimIndent(),
                protoVersion
            )
        }
    }

    @TestParameterInjectorTest
    fun `test WHEN enum uses a directly reserved number THEN a compilation exception is thrown`(
        @TestParameter protoVersion: ProtoVersion
    ) {
        assertThrows<CompilationException.ReservedFieldNumberUsed> {
            runGenerator(
                """
                    enum TestEnum {
                       reserved 1;
                       a = 0;
                       b = 1;
                    }
                """.trimIndent(),
                protoVersion
            )
        }
    }

    @TestParameterInjectorTest
    fun `test WHEN enum uses a reserved number in range THEN a compilation exception is thrown`(
        @TestParameter protoVersion: ProtoVersion
    ) {
        assertThrows<CompilationException.ReservedFieldNumberUsed> {
            runGenerator(
                """
                    enum TestEnum {
                       reserved 10 to 20;
                       a = 0;
                       b = 14;
                    }
                """.trimIndent(),
                protoVersion
            )
        }
    }

    @TestParameterInjectorTest
    fun `test WHEN enum uses a reserved field name THEN a compilation exception is thrown`(
        @TestParameter protoVersion: ProtoVersion
    ) {
        assertThrows<CompilationException.ReservedFieldNameUsed> {
            runGenerator(
                """
                    enum TestEnum {
                       reserved "b";
                       a = 0;
                       b = 1;
                    }
                """.trimIndent(),
                protoVersion
            )
        }
    }

    @TestParameterInjectorTest
    fun `test WHEN enum does not have default field THEN a compilation exception is thrown`(
        @TestParameter(value = ["PROTO3", "EDITION2023", "EDITION2024"]) protoVersion: ProtoVersion
    ) {
        assertThrows<CompilationException.EnumIllegalFirstField> {
            runGenerator(
                """
                    enum TestEnum {
                       field = 1;
                    }
                """.trimIndent(),
                protoVersion
            )
        }
    }

    @TestParameterInjectorTest
    fun `test WHEN enum has no field THEN compilation exception is thrown`(
        @TestParameter protoVersion: ProtoVersion
    ) {
        assertThrows<CompilationException.EnumNoFields> {
            runGenerator(
                """
                    enum TestEnum {
                        
                    }
                """.trimIndent(),
                protoVersion
            )
        }
    }

    @TestParameterInjectorTest
    fun `test WHEN enum has enum aliases but without the option THEN a warning is printed`(
        @TestParameter protoVersion: ProtoVersion
    ) {
        runGenerator(
            """
                    enum TestEnum {
                        field1 = 0;
                        field2 = 1;
                        field3 = 1;
                    }
                """.trimIndent(),
            protoVersion
        )

        verify(atLeast = 1) { logger.warn(matchWarning(Warnings.enumAliasWithoutOption)) }
    }

    @TestParameterInjectorTest
    fun `test WHEN enum has enum aliases and the option THEN no warning is printed`(
        @TestParameter protoVersion: ProtoVersion
    ) {
        runGenerator(
            """
                    enum TestEnum {
                        option allow_alias = true;
                        field1 = 0;
                        field2 = 1;
                        field3 = 1;
                    }
                """.trimIndent(),
            protoVersion
        )

        verify(atLeast = 0) { logger.warn(matchWarning(Warnings.enumAliasWithoutOption)) }
    }
}
