package o.github.timortel.kmpgrpc.composeexample

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform