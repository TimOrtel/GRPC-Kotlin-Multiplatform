package io.github.timortel.kotlin_multiplatform_grpc_plugin.test.model

import io.github.timortel.kmpgrpc.test.proto2.Proto2RequiredFields
import io.github.timortel.kmpgrpc.test.proto2.Proto2RequiredFields.Proto2MessageWithRequiredFields
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsInitializedTest {

    @Test
    fun testDefaultIsInitialized() {
        // invoke() uses default values that satisfy required fields
        val msg = Proto2MessageWithRequiredFields()
        assertTrue(msg.isInitialized, "Default message should be initialized")
    }

    @Test
    fun testMissingLocalRequiredField() {
        // field1 is required. Passing null via createPartial should make it uninitialized.
        val msg = Proto2MessageWithRequiredFields.createPartial(field1 = null)
        assertFalse(msg.isInitialized, "Message should be uninitialized if local required field is missing")
    }

    @Test
    fun testUninitializedNestedMessage() {
        // field2 is set, but the nested message itself is missing its own required field1
        val incompleteNested = Proto2RequiredFields.Proto2MessageWithMixedFields.createPartial(field1 = null)
        val msg = Proto2MessageWithRequiredFields.createPartial(
            field1 = "valid",
            field2 = incompleteNested
        )

        assertFalse(msg.isInitialized, "Message should be uninitialized if a nested message is uninitialized")
    }

    @Test
    fun testUninitializedMessageInList() {
        val incomplete = Proto2MessageWithRequiredFields.createPartial(field1 = null)
        val msg = Proto2MessageWithRequiredFields(
            field3List = listOf(incomplete)
        )

        assertFalse(msg.isInitialized, "Message should be uninitialized if any element in a repeated field is uninitialized")
    }

    @Test
    fun testUninitializedMessageInMap() {
        val incomplete = Proto2MessageWithRequiredFields.createPartial(field1 = null)
        val msg = Proto2MessageWithRequiredFields(
            field4Map = mapOf("key" to incomplete)
        )

        assertFalse(msg.isInitialized, "Message should be uninitialized if any value in a map is uninitialized")
    }

    @Test
    fun testOneOfInitialization() {
        // x.field5 is a message type. If that message is incomplete, the parent is incomplete.
        val incompleteMixed = Proto2RequiredFields.Proto2MessageWithMixedFields.createPartial(field1 = null)
        val msg = Proto2MessageWithRequiredFields(
            x = Proto2MessageWithRequiredFields.X.Field5(incompleteMixed)
        )

        assertFalse(msg.isInitialized, "Message should be uninitialized if a message inside a OneOf is uninitialized")

        // x.field6 is a string (primitive-like), so it's always considered initialized if the case is set
        val msg2 = Proto2MessageWithRequiredFields(
            x = Proto2MessageWithRequiredFields.X.Field6("hello")
        )
        assertTrue(msg2.isInitialized, "Message should be initialized if OneOf contains a valid string")
    }
}
