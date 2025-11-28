package io.github.timortel.kotlin_multiplatform_grpc_plugin.modeltree

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.declaration.message.field.ProtoMessageField
import io.github.timortel.kotlin_multiplatform_grpc_plugin.validation.BaseValidationTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class RepeatedFieldEncodingTest : BaseModelTreeTest() {

    @Test
    fun `test USING proto3 WHEN using default THEN encoding is PACKED`() {
        assertFieldEncoding(
            proto = """
            message TestMessage {
                repeated int32 a = 1;
            }
        """,
            version = BaseValidationTest.ProtoVersion.PROTO3,
            expectedPacked = true
        )
    }

    @Test
    fun `test USING proto3 WHEN setting is packed to true THEN encoding is PACKED`() {
        assertFieldEncoding(
            proto = """
            message TestMessage {
                repeated int32 a = 1 [packed = true];
            }
        """,
            version = BaseValidationTest.ProtoVersion.PROTO3,
            expectedPacked = true
        )
    }

    @Test
    fun `test USING proto3 WHEN setting is packed to false THEN encoding is not PACKED`() {
        assertFieldEncoding(
            proto = """
            message TestMessage {
                repeated int32 a = 1 [packed = false];
            }
        """,
            version = BaseValidationTest.ProtoVersion.PROTO3,
            expectedPacked = false
        )
    }

    @Test
    fun `test USING edition2023 WHEN using default THEN encoding is PACKED`() {
        assertFieldEncoding(
            proto = """
            message TestMessage {
                repeated int32 a = 1;
            }
        """,
            version = BaseValidationTest.ProtoVersion.EDITION2023,
            expectedPacked = true
        )
    }

    @Test
    fun `test USING edition2023 WHEN setting is packed to true THEN encoding is PACKED`() {
        assertFieldEncoding(
            proto = """
            message TestMessage {
                repeated int32 a = 1 [features.repeated_field_encoding = PACKED];
            }
        """,
            version = BaseValidationTest.ProtoVersion.EDITION2023,
            expectedPacked = true
        )
    }

    @Test
    fun `test USING edition2023 WHEN setting is EXPANDED THEN encoding is not PACKED`() {
        assertFieldEncoding(
            proto = """
            message TestMessage {
                repeated int32 a = 1 [features.repeated_field_encoding = EXPANDED];
            }
        """,
            version = BaseValidationTest.ProtoVersion.EDITION2023,
            expectedPacked = false
        )
    }


    private fun assertFieldEncoding(
        proto: String,
        version: BaseValidationTest.ProtoVersion,
        expectedPacked: Boolean
    ) {
        val project = buildProject(proto.trimIndent(), version)

        val field = project
            .findMessage("TestMessage")
            .findField("a")
            .assertIsInstance<ProtoMessageField>()

        Assertions.assertEquals(expectedPacked, field.isPacked, "Expected isPacked to match")
    }
}
