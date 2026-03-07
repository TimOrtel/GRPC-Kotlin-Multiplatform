package io.github.timortel.kotlin_multiplatform_grpc_plugin.test.model

import io.github.timortel.kmpgrpc.core.message.extensions.buildExtensions
import io.github.timortel.kmpgrpc.test.proto2.Proto2RequiredFields
import io.github.timortel.kmpgrpc.test.proto2.Proto2RequiredFields.Proto2MessageWithMixedFields
import io.github.timortel.kmpgrpc.test.proto2.Proto2RequiredFields.Proto2MessageWithRequiredExtension
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
        val incompleteNested = Proto2MessageWithMixedFields.createPartial(field1 = null)
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

        assertFalse(
            msg.isInitialized,
            "Message should be uninitialized if any element in a repeated field is uninitialized"
        )
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
        val incompleteMixed = Proto2MessageWithMixedFields.createPartial(field1 = null)
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

    @Test
    fun testRequiredMessageExtensionInitialization() {
        // 1. Missing both required extensions
        val emptyMsg = Proto2MessageWithRequiredExtension.createPartial()
        assertFalse(emptyMsg.isInitialized, "Should be uninitialized: missing extension1 and extensionRequiredMsg")

        // 2. extension1 is present, but extensionRequiredMsg is missing
        val partialExt1 = buildExtensions {
            set(Proto2RequiredFields.extension1, "valid")
        }
        val msgOnlyExt1 = Proto2MessageWithRequiredExtension.createPartial(extensions = partialExt1)
        assertFalse(msgOnlyExt1.isInitialized, "Should be uninitialized: missing required message extension")

        // 3. Both present, but the required message extension is itself uninitialized
        val incompleteNested = Proto2MessageWithMixedFields.createPartial(field1 = null)
        val partialExt2 = buildExtensions {
            set(Proto2RequiredFields.extension1, "valid")
            set(Proto2RequiredFields.extensionRequiredMsg, incompleteNested)
        }
        val msgIncompleteMsg = Proto2MessageWithRequiredExtension.createPartial(extensions = partialExt2)
        assertFalse(
            msgIncompleteMsg.isInitialized,
            "Should be uninitialized: required message extension is missing field1"
        )

        // 4. Fully initialized
        val completeExt = buildExtensions {
            set(Proto2RequiredFields.extension1, "valid")
            set(Proto2RequiredFields.extensionRequiredMsg, Proto2MessageWithMixedFields(field1 = "valid"))
        }
        val validMsg = Proto2MessageWithRequiredExtension(extensions = completeExt)
        assertTrue(
            validMsg.isInitialized,
            "Should be initialized: all required extensions and their fields are present"
        )
    }

    @Test
    fun testRepeatedMessageExtensionInitialization() {
        val validNested = Proto2MessageWithMixedFields(field1 = "ok")
        val incompleteNested = Proto2MessageWithMixedFields.createPartial(field1 = null)

        // Base valid extensions so the parent's 'required' constraints are met
        val baseExtensions = buildExtensions {
            set(Proto2RequiredFields.extension1, "valid")
            set(Proto2RequiredFields.extensionRequiredMsg, validNested)
            set(Proto2RequiredFields.extensionRepeatedMsgList, listOf(validNested, incompleteNested))
        }

        val msg = Proto2MessageWithRequiredExtension(extensions = baseExtensions)

        assertFalse(
            msg.isInitialized,
            "Should be uninitialized: one element in the repeated message extension is uninitialized"
        )
    }
}
