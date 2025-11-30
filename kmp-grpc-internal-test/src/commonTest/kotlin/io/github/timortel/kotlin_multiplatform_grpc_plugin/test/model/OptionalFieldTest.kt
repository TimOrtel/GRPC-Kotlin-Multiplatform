package io.github.timortel.kotlin_multiplatform_grpc_plugin.test.model

import io.github.timortel.kmpgrpc.test.OptionalScalarFields
import kotlin.test.Test
import kotlin.test.assertEquals

class OptionalFieldTest {

    private val messageWithValues = OptionalScalarFields(
        field1 = 1.23,
        field2 = 4.56f,
        field3 = 123,
        field4 = 123456789L,
        field5 = 123u,
        field6 = 1234567890123456789uL,
        field7 = -42,
        field8 = -9876543210L,
        field9 = 999u,
        field10 = 999999999999uL,
        field11 = -9999,
        field12 = -999999999999L,
        field13 = true,
        field14 = "example",
        field15 = byteArrayOf(0x01, 0x02)
    )

    @Test
    fun testOptionalFieldsByDefaultNotSet() {
        val msg = OptionalScalarFields()
        checkAllFieldsSet(msg, false)
    }

    @Test
    fun testOptionalFieldsAreSetWhenSet() {
        checkAllFieldsSet(messageWithValues, true)
    }

    @Test
    fun testOptionalFieldsAfterSerializingStillNotSet() {
        val msg = OptionalScalarFields()
        val newMsg = OptionalScalarFields.deserialize(msg.serialize())
        checkAllFieldsSet(newMsg, false)
    }

    @Test
    fun testOptionalFieldsAfterSerializingStillSet() {
        val msg = messageWithValues
        val newMsg = OptionalScalarFields.deserialize(msg.serialize())
        checkAllFieldsSet(newMsg, true)
    }

    private fun checkAllFieldsSet(msg: OptionalScalarFields, set: Boolean) {
        val expectation = if (set) "must be set" else "must not be set"

        assertEquals(set, msg.isField1Set, "Field1 $expectation")
        assertEquals(set, msg.isField2Set, "Field2 $expectation")
        assertEquals(set, msg.isField3Set, "Field3 $expectation")
        assertEquals(set, msg.isField4Set, "Field4 $expectation")
        assertEquals(set, msg.isField5Set, "Field5 $expectation")
        assertEquals(set, msg.isField6Set, "Field6 $expectation")
        assertEquals(set, msg.isField7Set, "Field7 $expectation")
        assertEquals(set, msg.isField8Set, "Field8 $expectation")
        assertEquals(set, msg.isField9Set, "Field9 $expectation")
        assertEquals(set, msg.isField10Set, "Field10 $expectation")
        assertEquals(set, msg.isField11Set, "Field11 $expectation")
        assertEquals(set, msg.isField12Set, "Field12 $expectation")
        assertEquals(set, msg.isField13Set, "Field13 $expectation")
        assertEquals(set, msg.isField14Set, "Field14 $expectation")
        assertEquals(set, msg.isField15Set, "Field15 $expectation")
    }
}
