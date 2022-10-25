package io.github.timortel.kotlin_multiplatform_grpc_lib

import io.github.timortel.kotlin_multiplatform_grpc_lib.util.TimeUnit

actual class KMChannel private constructor(
    private val name: String,
    private val port: Int,
    private val usePlainText: Boolean,
    val metadata: KMMetadata
) {

    val connectionString = (if (usePlainText) "http://" else "https://") + "$name:$port"

    actual data class Builder(val name: String, val port: Int) {

        private var usePlainText: Boolean = false

        actual companion object {
            actual fun forAddress(
                name: String,
                port: Int
            ): Builder = Builder(name, port)
        }

        actual fun usePlaintext(): Builder {
            usePlainText = true
            return this
        }

        actual fun build(): KMChannel =
            io.github.timortel.kotlin_multiplatform_grpc_lib.KMChannel(name, port, usePlainText)
    }

    actual fun withDeadlineAfter(
        duration: Long,
        unit: TimeUnit
    ): KMChannel {
        val newMetadata = metadata.copy()
        newMetadata["deadline"] = (duration * unit.toMilliFactor).toString()

        return KMChannel(name, port, usePlainText, newMetadata)
    }
}