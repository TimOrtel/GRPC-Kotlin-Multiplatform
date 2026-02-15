package io.github.timortel.kotlin_multiplatform_grpc_plugin.test.model

import io.github.timortel.kmpgrpc.core.UninitializedMessageException
import io.github.timortel.kmpgrpc.test.proto2.Proto2RequiredFields
import io.github.timortel.kmpgrpc.test.proto2.proto2MessageWithMixedFields
import io.github.timortel.kmpgrpc.test.proto2.proto2MessageWithRequiredFields
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class UninitializedBuilderTest {

    @Test
    fun testSuccessfulBuild() {
        // Should not throw because all required fields are set
        val msg = proto2MessageWithRequiredFields {
            field1 = "top level"
            field2 = proto2MessageWithMixedFields {
                field1 = "nested required"
            }
        }
        assertNotNull(msg)
    }

    @Test
    fun testMissingTopLevelRequiredField() {
        assertFailsWith<UninitializedMessageException>("Should throw if top-level required field1 is missing") {
            proto2MessageWithRequiredFields {
                // field1 is missing
                field2 = proto2MessageWithMixedFields { field1 = "valid" }
            }
        }
    }

    @Test
    fun testMissingNestedRequiredField() {
        assertFailsWith<UninitializedMessageException>("Should throw if a required field inside field2 is missing") {
            proto2MessageWithRequiredFields {
                field1 = "valid"
                field2 = proto2MessageWithMixedFields {
                    // field1 is required in MixedFields but missing here
                    field2 = 123
                }
            }
        }
    }

    @Test
    fun testUninitializedInList() {
        assertFailsWith<UninitializedMessageException>("Should throw if an element in the list is uninitialized") {
            proto2MessageWithRequiredFields {
                field1 = "valid"
                field2 = proto2MessageWithMixedFields { field1 = "valid" }

                // Add an incomplete message to the list
                field3List.add(Proto2RequiredFields.Proto2MessageWithRequiredFields.createPartial(field1 = null))
            }
        }
    }

    @Test
    fun testUninitializedInMap() {
        assertFailsWith<UninitializedMessageException>("Should throw if a map value is uninitialized") {
            proto2MessageWithRequiredFields {
                field1 = "valid"
                field2 = proto2MessageWithMixedFields { field1 = "valid" }

                // Add an incomplete message to the map
                field4Map["key"] = Proto2RequiredFields.Proto2MessageWithRequiredFields.createPartial(field1 = null)
            }
        }
    }

    @Test
    fun testUninitializedInOneOf() {
        assertFailsWith<UninitializedMessageException>("Should throw if the chosen OneOf case is uninitialized") {
            proto2MessageWithRequiredFields {
                field1 = "valid"
                field2 = proto2MessageWithMixedFields { field1 = "valid" }

                // x is set to a Field5 which contains an uninitialized message
                x = Proto2RequiredFields.Proto2MessageWithRequiredFields.X.Field5(
                    Proto2RequiredFields.Proto2MessageWithMixedFields.createPartial(field1 = null)
                )
            }
        }
    }
}
