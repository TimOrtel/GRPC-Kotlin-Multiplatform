package io.github.timortel.kmpgrpc

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform