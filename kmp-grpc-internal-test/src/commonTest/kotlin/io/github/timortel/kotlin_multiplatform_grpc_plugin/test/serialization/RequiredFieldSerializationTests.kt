package io.github.timortel.kotlin_multiplatform_grpc_plugin.test.serialization

import io.github.timortel.kmpgrpc.test.proto2.Proto2RequiredFields.Proto2MessageWithMixedFields
import io.github.timortel.kmpgrpc.test.proto2.Proto2RequiredFields.Proto2MessageWithRequiredFields
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RequiredFieldSerializationTests {

    @Test
    fun testPartialMessageRoundTripRemainsUninitialized() {
        // 1. Create a partial message missing a required field (field1)
        val original = Proto2MessageWithRequiredFields.createPartial(
            field1 = null,
            field2 = Proto2MessageWithMixedFields("valid")
        )
        assertFalse(original.isInitialized, "Original should be uninitialized")

        // 2. Serialize to bytes
        val bytes = original.serialize()

        // 3. Deserialize back
        val deserialized = Proto2MessageWithRequiredFields.deserialize(bytes)

        // 4. Verify state is preserved
        assertFalse(deserialized.isInitialized, "Deserialized message should still be uninitialized")
        assertEquals(original.field2, deserialized.field2, "Other data should remain intact")
    }

    @Test
    fun testNestedUninitializedMessageRoundTrip() {
        // 1. Create a message where the parent is "complete" but the child is "partial"
        val partialChild = Proto2MessageWithMixedFields.createPartial(field1 = null)
        val original = Proto2MessageWithRequiredFields.createPartial(
            field1 = "parent-valid",
            field2 = partialChild
        )
        assertFalse(original.isInitialized, "Parent should be uninitialized because child is uninitialized")

        // 2. Round trip
        val bytes = original.serialize()
        val deserialized = Proto2MessageWithRequiredFields.deserialize(bytes)

        // 3. Verify
        assertFalse(deserialized.isInitialized, "Deserialized parent should still be uninitialized")
        assertFalse(deserialized.field2.isInitialized, "Deserialized child should still be uninitialized")
    }

    @Test
    fun testFullyInitializedRoundTrip() {
        // 1. Create a fully valid message
        val original = Proto2MessageWithRequiredFields(
            field1 = "valid",
            field2 = Proto2MessageWithMixedFields(field1 = "nested-valid")
        )
        assertTrue(original.isInitialized)

        // 2. Round trip
        val bytes = original.serialize()
        val deserialized = Proto2MessageWithRequiredFields.deserialize(bytes)

        // 3. Verify
        assertTrue(deserialized.isInitialized, "Deserialized message should be fully initialized")
        assertEquals("valid", deserialized.field1)
    }

    @Test
    fun testEmptyRepeatedAndMapRoundTrip() {
        // In proto2, empty repeated/map fields are initialized by default
        // as long as the local required fields are present.
        val original = Proto2MessageWithRequiredFields.createPartial(
            field1 = "valid",
            field2 = Proto2MessageWithMixedFields("valid"),
            field3List = emptyList(),
            field4Map = emptyMap()
        )

        assertTrue(original.isInitialized)

        val deserialized = Proto2MessageWithRequiredFields.deserialize(original.serialize())
        assertTrue(deserialized.isInitialized, "Message with empty collections should stay initialized")
    }
}
