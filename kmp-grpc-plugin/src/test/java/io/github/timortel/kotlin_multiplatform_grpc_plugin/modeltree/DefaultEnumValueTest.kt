package io.github.timortel.kotlin_multiplatform_grpc_plugin.modeltree

import com.google.testing.junit.testparameterinjector.junit5.TestParameter
import com.google.testing.junit.testparameterinjector.junit5.TestParameterInjectorTest
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMessageField
import io.github.timortel.kotlin_multiplatform_grpc_plugin.validation.BaseValidationTest
import org.junit.jupiter.api.Assertions

class DefaultEnumValueTest : BaseModelTreeTest() {

    @TestParameterInjectorTest
    fun `test USING proto langauge version WHEN proto enum field is used without default value THEN the first entry is the default value`(
        @TestParameter version: BaseValidationTest.ProtoVersion
    ) {
        val fieldPrefix = when (version) {
            BaseValidationTest.ProtoVersion.PROTO2 -> "required"
            else -> ""
        }

        assertDefaultEnumValue(
            proto = """
                ${if (version == BaseValidationTest.ProtoVersion.EDITION2024) "option features.(pb.java).nest_in_file_class = YES;" else ""}
                
                enum A {
                    A = 0;
                    B = 1;
                }
                
                message C {
                    $fieldPrefix A a = 1;
                }
            """,
            version = version,
            expectedDefaultValue = "A.A"
        )
    }

    @TestParameterInjectorTest
    fun `test USING langauge version WHEN proto enum field is used with default value THEN the correct default value is chosen`(
        @TestParameter(value = ["PROTO2", "EDITION2023", "EDITION2024"]) version: BaseValidationTest.ProtoVersion
    ) {
        val fieldPrefix = when (version) {
            BaseValidationTest.ProtoVersion.PROTO2 -> "required"
            else -> ""
        }

        assertDefaultEnumValue(
            proto = """
                ${if (version == BaseValidationTest.ProtoVersion.EDITION2024) "option features.(pb.java).nest_in_file_class = YES;" else ""}
                enum A {
                    A = 0;
                    B = 1;
                }
                
                message C {
                    $fieldPrefix A a = 1 [default = B];
                }
            """,
            version = version,
            expectedDefaultValue = "A.B"
        )
    }

    private fun assertDefaultEnumValue(
        proto: String,
        version: BaseValidationTest.ProtoVersion,
        expectedDefaultValue: String
    ) {
        val project = buildProject(proto.trimIndent(), version)

        val field = project
            .findMessage("C")
            .findField("a")
            .assertIsInstance<ProtoMessageField>()

        val defaultValueCode = field.defaultValue().toString()

        Assertions.assertEquals("TestFile.$expectedDefaultValue", defaultValueCode)
    }
}
