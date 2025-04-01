package io.github.timortel.kmpgrpc.wkt.ext

import com.google.protobuf.Any
import io.github.timortel.test.Testmessages
import io.github.timortel.test.a
import io.github.timortel.test.b
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnyTest {

    @Test
    fun `test WHEN packing a message THEN unpacking the message yields an identical message`() {
        val expected = a {
            someString = "Test"
            someBool = true

            someB = b {
                someInt = 24
            }
        }

        val actual = Any.wrap(expected).unwrap(Testmessages.A.Companion)

        assertEquals(expected, actual)
    }

    @Test
    fun `test GIVEN an packed message WHEN checking for the type with the original message THEN true is returned`() {
        val expected = a {
            someString = "Test"
            someBool = true

            someB = b {
                someInt = 24
            }
        }

        val wrapped = Any.wrap(expected)

        assertTrue(wrapped.isType(Testmessages.A.Companion))
        assertTrue(wrapped.isSameTypeAs(expected))
    }

    @Test
    fun `test GIVEN an packed message WHEN checking for the type with a different message THEN false is returned`() {
        val expected = a {
            someString = "Test"
            someBool = true

            someB = b {
                someInt = 24
            }
        }

        val actual = b {}

        val wrapped = Any.wrap(expected)

        assertFalse(wrapped.isType(Testmessages.B.Companion))
        assertFalse(wrapped.isSameTypeAs(actual))
    }
}
