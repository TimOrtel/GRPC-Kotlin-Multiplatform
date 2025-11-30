package io.github.timortel.kotlin_multiplatform_grpc_plugin.modeltree

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMessageField
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.isExplicit
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.isImplicit
import io.github.timortel.kotlin_multiplatform_grpc_plugin.validation.BaseValidationTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class FieldPresenceTest : BaseModelTreeTest() {

    @Test
    fun `test USING proto3 WHEN singular field has no optional THEN field presence is IMPLICIT`() {
        assertFieldPresence(
            proto = """
            message TestMessage {
                string a = 1;
            }
        """,
            version = BaseValidationTest.ProtoVersion.PROTO3,
            expectedImplicit = true
        )
    }

    @Test
    fun `test USING proto3 WHEN singular field is optional THEN presence is EXPLICIT`() {
        assertFieldPresence(
            proto = """
            message TestMessage {
                optional string a = 1;
            }
        """,
            version = BaseValidationTest.ProtoVersion.PROTO3,
            expectedImplicit = false
        )
    }

    @Test
    fun `test USING editions2023 WHEN setting nothing THEN presence is EXPLICIT`() {
        assertFieldPresence(
            proto = """
            message TestMessage {
                string a = 1;
            }
        """,
            version = BaseValidationTest.ProtoVersion.EDITION2023,
            expectedImplicit = false
        )
    }

    @Test
    fun `test USING editions2023 WHEN setting feature to IMPLICIT THEN presence is IMPLICIT`() {
        assertFieldPresence(
            proto = """
            option features.field_presence = IMPLICIT;
            message TestMessage {
                string a = 1;
            }
        """,
            version = BaseValidationTest.ProtoVersion.EDITION2023,
            expectedImplicit = true
        )
    }

    @Test
    fun `test USING editions2023 WHEN setting feature to EXPLICIT THEN presence is EXPLICIT`() {
        assertFieldPresence(
            proto = """
            option features.field_presence = EXPLICIT;
            message TestMessage {
                string a = 1;
            }
        """,
            version = BaseValidationTest.ProtoVersion.EDITION2023,
            expectedImplicit = false
        )
    }

    private fun assertFieldPresence(
        proto: String,
        version: BaseValidationTest.ProtoVersion,
        expectedImplicit: Boolean
    ) {
        val project = buildProject(proto.trimIndent(), version)

        val field = project
            .findMessage("TestMessage")
            .findField("a")
            .assertIsInstance<ProtoMessageField>()

        if (expectedImplicit) {
            Assertions.assertTrue(
                field.cardinality.isImplicit,
                "Expected field presence to be IMPLICIT"
            )
        } else {
            Assertions.assertTrue(
                field.cardinality.isExplicit,
                "Expected field presence to be EXPLICIT"
            )
        }
    }
}
