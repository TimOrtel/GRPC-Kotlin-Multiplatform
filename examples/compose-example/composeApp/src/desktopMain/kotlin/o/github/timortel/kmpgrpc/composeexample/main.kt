package o.github.timortel.kmpgrpc.composeexample

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "compose-example",
    ) {
        App()
    }
}