package io.github.timortel.kmpgrpc.wkt.ext

import com.google.protobuf.*

public fun DoubleValue.Companion.of(value: Double): DoubleValue = doubleValue { this.value = value }

public fun FloatValue.Companion.of(value: Float): FloatValue = floatValue { this.value = value }

public fun Int64Value.Companion.of(value: Long): Int64Value = int64Value { this.value = value }

public fun UInt64Value.Companion.of(value: ULong): UInt64Value = uInt64Value { this.value = value }

public fun Int32Value.Companion.of(value: Int): Int32Value = int32Value { this.value = value }

public fun UInt32Value.Companion.of(value: UInt): UInt32Value = uInt32Value { this.value = value }

public fun BoolValue.Companion.of(value: Boolean): BoolValue = boolValue { this.value = value }

public fun StringValue.Companion.of(value: String): StringValue = stringValue { this.value = value }

public fun BytesValue.Companion.of(value: ByteArray): BytesValue = bytesValue { this.value = value }
