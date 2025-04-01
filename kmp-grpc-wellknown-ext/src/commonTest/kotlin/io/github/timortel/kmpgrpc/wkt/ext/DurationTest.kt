package io.github.timortel.kmpgrpc.wkt.ext

import com.google.protobuf.Duration
import com.google.protobuf.duration
import kotlin.test.Test
import kotlin.test.assertEquals

class DurationTest {

    @Test
    fun testConversion() {
        conversionTest(
            duration {
                seconds = 12
                nanos = 41
            }
        )

        conversionTest(
            duration {
                seconds = -12
                nanos = -14
            }
        )
    }

    private fun conversionTest(original: Duration) {
        val converted = Duration.fromDuration(original.toDuration())

        assertEquals(original, converted)
    }
}
