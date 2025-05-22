package io.github.timortel.kmpgrpc.composeexample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "compose-example",
    ) {
        App(modifier = Modifier.fillMaxSize())
    }
}