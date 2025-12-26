package io.github.timortel.kotlin_multiplatform_grpc_plugin.validation

import com.google.testing.junit.testparameterinjector.junit5.TestParameter
import com.google.testing.junit.testparameterinjector.junit5.TestParameterInjectorTest
import com.google.testing.junit.testparameterinjector.junit5.TestParameterValuesProvider
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.CompilationException
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoDefaultSymbolVisibility
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.ProtoSymbolVisibility
import io.github.timortel.kotlin_multiplatform_grpc_plugin.FakeInputDirectory
import io.github.timortel.kotlin_multiplatform_grpc_plugin.createProtoFile
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.reflect.KClass

class ExportValidationTests : BaseValidationTest() {

    @Suppress("unused")
    enum class VisibilityLangaugeVersionScenario(
        val version: ProtoVersion,
        val visibility: ProtoSymbolVisibility?,
        val expectException: Boolean
    ) {
        EDITION2023_1(ProtoVersion.EDITION2023, null, false),
        EDITION2023_2(ProtoVersion.EDITION2023, ProtoSymbolVisibility.EXPORT, true),
        EDITION2023_3(ProtoVersion.EDITION2023, ProtoSymbolVisibility.LOCAL, true),
        EDITION2024_1(ProtoVersion.EDITION2024, null, false),
        EDITION2024_2(ProtoVersion.EDITION2024, ProtoSymbolVisibility.EXPORT, false),
        EDITION2024_3(ProtoVersion.EDITION2024, ProtoSymbolVisibility.LOCAL, false)
    }

    @TestParameterInjectorTest
    fun `test GIVEN language version WHEN applying export and local modifiers THEN language rules are enforced`(
        @TestParameter scenario: VisibilityLangaugeVersionScenario
    ) {
        val visibility = when (scenario.visibility) {
            ProtoSymbolVisibility.LOCAL -> "local"
            ProtoSymbolVisibility.EXPORT -> "export"
            null -> ""
        }

        val exec = {
            runGenerator(
                """
                $visibility message A {
                }
            """.trimIndent(),
                protoVersion = scenario.version
            )
        }

        if (scenario.expectException) {
            assertThrows<CompilationException.UnsupportedLanguageFeatureUsed> {
                exec()
            }
        } else {
            exec()
        }
    }

    @TestParameterInjectorTest
    fun `test GIVEN default symbol visibility WHEN importing top level declaration THEN no warning or error is printed`(
        @TestParameter version: ProtoVersion
    ) {
        runDefaultGenerator(
            file1 = """
                message A {}
            """,
            version = version
        )

        verify(exactly = 0) { logger.warn(any()) }
    }

    @Test
    fun `test GIVEN edition 2024 WHEN importing exported top level declaration THEN no warning or error is printed`() {
        runDefaultGenerator(
            file1 = """
                export message A {}
            """,
            version = ProtoVersion.EDITION2024
        )

        verify(exactly = 0) { logger.warn(any()) }
    }

    data class VisibilityTestScenario(
        val defaultVisibility: ProtoDefaultSymbolVisibility?,
        val declarationModifier: ProtoSymbolVisibility?,
        val exceptionClass: KClass<out Throwable>?
    )

    private class VisibilityScenarioProvider : TestParameterValuesProvider() {

        override fun provideValues(context: Context): List<Any> =
            listOf(
                // NONE
                VisibilityTestScenario(
                    null,
                    null,
                    exceptionClass = null, // bare top-level is exported
                ),
                VisibilityTestScenario(
                    null,
                    ProtoSymbolVisibility.EXPORT,
                    exceptionClass = null, // explicit export
                ),
                VisibilityTestScenario(
                    null,
                    ProtoSymbolVisibility.LOCAL,
                    exceptionClass = CompilationException.ImportLocalDeclaration::class,  // explicit local wins
                ),

                // EXPORT_ALL
                VisibilityTestScenario(
                    ProtoDefaultSymbolVisibility.EXPORT_ALL,
                    null,
                    exceptionClass = null, // bare top-level is exported
                ),
                VisibilityTestScenario(
                    ProtoDefaultSymbolVisibility.EXPORT_ALL,
                    ProtoSymbolVisibility.EXPORT,
                    exceptionClass = null, // explicit export
                ),
                VisibilityTestScenario(
                    ProtoDefaultSymbolVisibility.EXPORT_ALL,
                    ProtoSymbolVisibility.LOCAL,
                    exceptionClass = CompilationException.ImportLocalDeclaration::class,  // explicit local wins
                ),

                // EXPORT_TOP_LEVEL
                VisibilityTestScenario(
                    ProtoDefaultSymbolVisibility.EXPORT_TOP_LEVEL,
                    null,
                    exceptionClass = null, // bare top-level is exported
                ),
                VisibilityTestScenario(
                    ProtoDefaultSymbolVisibility.EXPORT_TOP_LEVEL,
                    ProtoSymbolVisibility.EXPORT,
                    exceptionClass = null, // explicit export
                ),
                VisibilityTestScenario(
                    ProtoDefaultSymbolVisibility.EXPORT_TOP_LEVEL,
                    ProtoSymbolVisibility.LOCAL,
                    exceptionClass = CompilationException.ImportLocalDeclaration::class,  // explicit local
                ),

                // LOCAL_ALL
                VisibilityTestScenario(
                    ProtoDefaultSymbolVisibility.LOCAL_ALL,
                    null,
                    exceptionClass = CompilationException.ImportLocalDeclaration::class,  // bare top-level is local
                ),
                VisibilityTestScenario(
                    ProtoDefaultSymbolVisibility.LOCAL_ALL,
                    ProtoSymbolVisibility.EXPORT,
                    exceptionClass = null, // explicit export overrides default
                ),
                VisibilityTestScenario(
                    ProtoDefaultSymbolVisibility.LOCAL_ALL,
                    ProtoSymbolVisibility.LOCAL,
                    exceptionClass = CompilationException.ImportLocalDeclaration::class,  // explicit local
                ),

                // STRICT
                VisibilityTestScenario(
                    ProtoDefaultSymbolVisibility.STRICT,
                    null,
                    exceptionClass = CompilationException.ImportLocalDeclaration::class,  // bare top-level is local
                ),
                VisibilityTestScenario(
                    ProtoDefaultSymbolVisibility.STRICT,
                    ProtoSymbolVisibility.EXPORT,
                    exceptionClass = null, // explicit export
                ),
                VisibilityTestScenario(
                    ProtoDefaultSymbolVisibility.STRICT,
                    ProtoSymbolVisibility.LOCAL,
                    exceptionClass = CompilationException.ImportLocalDeclaration::class,  // explicit local
                )
            )
    }

    @TestParameterInjectorTest
    fun `test GIVEN edition 2024 WHEN importing top level declaration THEN visibility rules are enforced`(
        @TestParameter(valuesProvider = VisibilityScenarioProvider::class)
        scenario: VisibilityTestScenario
    ) {
        val optionLine = if (scenario.defaultVisibility != null) {
            "option features.default_symbol_visibility = \"${scenario.defaultVisibility.name}\";"
        } else ""

        val declarationLine = when (scenario.declarationModifier) {
            null -> "message A {}"
            ProtoSymbolVisibility.EXPORT -> "export message A {}"
            ProtoSymbolVisibility.LOCAL -> "local message A {}"
        }

        val file1 = """
            $optionLine
            
            $declarationLine
        """

        if (scenario.exceptionClass != null) {
            Assertions.assertThrows(scenario.exceptionClass.java) {
                runDefaultGenerator(
                    file1 = file1,
                    version = ProtoVersion.EDITION2024
                )
            }
        } else {
            runDefaultGenerator(
                file1 = file1,
                version = ProtoVersion.EDITION2024
            )
        }
    }

    class NestedVisibilityScenarioProvider : TestParameterValuesProvider() {

        override fun provideValues(context: Context): List<Any> =
            listOf(
                // ===== NONE =====
                VisibilityTestScenario(
                    null,
                    null,
                    exceptionClass = CompilationException.ImportLocalDeclaration::class,
                ),
                VisibilityTestScenario(
                    null,
                    ProtoSymbolVisibility.EXPORT,
                    exceptionClass = null,
                ),
                VisibilityTestScenario(
                    null,
                    ProtoSymbolVisibility.LOCAL,
                    exceptionClass = CompilationException.ImportLocalDeclaration::class,
                ),

                // ===== EXPORT_ALL =====
                // All symbols exported by default (including nested), unless explicitly local.
                VisibilityTestScenario(
                    ProtoDefaultSymbolVisibility.EXPORT_ALL,
                    null,
                    exceptionClass = null, // nested A is exported by default
                ),
                VisibilityTestScenario(
                    ProtoDefaultSymbolVisibility.EXPORT_ALL,
                    ProtoSymbolVisibility.EXPORT,
                    exceptionClass = null, // explicit export
                ),
                VisibilityTestScenario(
                    ProtoDefaultSymbolVisibility.EXPORT_ALL,
                    ProtoSymbolVisibility.LOCAL,
                    exceptionClass = CompilationException.ImportLocalDeclaration::class,  // explicit local
                ),

                // ===== EXPORT_TOP_LEVEL =====
                // Only top-level are exported by default; nested are local unless explicitly exported.
                VisibilityTestScenario(
                    ProtoDefaultSymbolVisibility.EXPORT_TOP_LEVEL,
                    null,
                    exceptionClass = CompilationException.ImportLocalDeclaration::class,  // nested A is local by default
                ),
                VisibilityTestScenario(
                    ProtoDefaultSymbolVisibility.EXPORT_TOP_LEVEL,
                    ProtoSymbolVisibility.EXPORT,
                    exceptionClass = null, // explicit export on nested
                ),
                VisibilityTestScenario(
                    ProtoDefaultSymbolVisibility.EXPORT_TOP_LEVEL,
                    ProtoSymbolVisibility.LOCAL,
                    exceptionClass = CompilationException.ImportLocalDeclaration::class,  // explicit local
                ),

                // ===== LOCAL_ALL =====
                // Everything local by default; explicit export required, including nested.
                VisibilityTestScenario(
                    ProtoDefaultSymbolVisibility.LOCAL_ALL,
                    null,
                    exceptionClass = CompilationException.ImportLocalDeclaration::class,  // nested A is local by default
                ),
                VisibilityTestScenario(
                    ProtoDefaultSymbolVisibility.LOCAL_ALL,
                    ProtoSymbolVisibility.EXPORT,
                    exceptionClass = null, // explicit export
                ),
                VisibilityTestScenario(
                    ProtoDefaultSymbolVisibility.LOCAL_ALL,
                    ProtoSymbolVisibility.LOCAL,
                    exceptionClass = CompilationException.ImportLocalDeclaration::class,  // explicit local
                ),

                // ===== STRICT =====
                // All symbols local by default. Nested types cannot be exported (A is always local).
                VisibilityTestScenario(
                    ProtoDefaultSymbolVisibility.STRICT,
                    null,
                    exceptionClass = CompilationException.ImportLocalDeclaration::class,  // nested A local
                ),
                VisibilityTestScenario(
                    ProtoDefaultSymbolVisibility.STRICT,
                    ProtoSymbolVisibility.EXPORT,
                    exceptionClass = CompilationException.StrictExportViolation::class,  // export on nested does not make it visible
                ),
                VisibilityTestScenario(
                    ProtoDefaultSymbolVisibility.STRICT,
                    ProtoSymbolVisibility.LOCAL,
                    exceptionClass = CompilationException.ImportLocalDeclaration::class,  // explicit local
                ),
            )
    }

    @TestParameterInjectorTest
    fun `test GIVEN edition 2024 WHEN importing nested declaration THEN visibility rules are enforced`(
        @TestParameter(valuesProvider = NestedVisibilityScenarioProvider::class)
        scenario: VisibilityTestScenario
    ) {
        val optionLine = if (scenario.defaultVisibility != null) {
            "option features.default_symbol_visibility = \"${scenario.defaultVisibility.name}\";"
        } else ""

        val nestedDeclaration = when (scenario.declarationModifier) {
            null -> """
                message C {
                    message A {}
                }
            """

            ProtoSymbolVisibility.EXPORT -> """
                message C {
                    export message A {}
                }
            """

            ProtoSymbolVisibility.LOCAL -> """
                message C {
                    local message A {}
                }
            """
        }.trimIndent()

        val file1 = """
            $optionLine
            
            $nestedDeclaration
        """

        val file2 = """
            import "file1.proto";
            
            message B {
                C.A a = 1;
            }
        """

        if (scenario.exceptionClass != null) {
            Assertions.assertThrows(scenario.exceptionClass.java) {
                runDefaultGenerator(
                    file1 = file1,
                    file2 = file2,
                    version = ProtoVersion.EDITION2024
                )
            }
        } else {
            runDefaultGenerator(
                file1 = file1,
                file2 = file2,
                version = ProtoVersion.EDITION2024
            )
        }
    }

    private fun runDefaultGenerator(
        file1: String,
        file2: String = """
            import "file1.proto";
            
            message B {
                A a = 1;
            }
        """,
        version: ProtoVersion
    ) {
        runGenerator(
            folder = listOf(
                FakeInputDirectory(
                    name = "dir",
                    files = listOf(
                        createProtoFile(version.header, file1.trimIndent(), name = "file1.proto"),
                        createProtoFile(version.header, file2.trimIndent(), name = "file2.proto"),
                    )
                )
            )
        )
    }
}
