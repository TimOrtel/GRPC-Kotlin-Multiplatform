package io.github.timortel.kmpgrpc.core

class Certificate internal constructor(val pemContent: String) {
    companion object {
        fun fromPem(content: String): Certificate {
            return Certificate(content)
        }
    }
}
