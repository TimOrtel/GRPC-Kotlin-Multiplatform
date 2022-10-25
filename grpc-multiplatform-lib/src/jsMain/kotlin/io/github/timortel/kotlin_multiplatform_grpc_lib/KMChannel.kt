package io.github.timortel.kotlin_multiplatform_grpc_lib


actual class KMChannel private constructor(
    name: String,
    port: Int,
    usePlainText: Boolean,
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

        actual fun build(): KMChannel = KMChannel(name, port, usePlainText, KMMetadata())
    }
}