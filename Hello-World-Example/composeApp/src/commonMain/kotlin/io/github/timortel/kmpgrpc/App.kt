package io.github.timortel.kmpgrpc

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import hello_world_example.composeapp.generated.resources.Res
import hello_world_example.composeapp.generated.resources.compose_multiplatform

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { showContent = !showContent }) {
                Text("Click me!")
            }

            AnimatedVisibility(showContent) {
                // Async state for the greeting call
                var loading by remember { mutableStateOf(false) }
                var greeting by remember { mutableStateOf<String?>(null) }
                var error by remember { mutableStateOf<Throwable?>(null) }

                LaunchedEffect(showContent) {
                    // This block runs when the content becomes visible
                    loading = true
                    greeting = null
                    error = null
                    try {
                        greeting = Greeting().greet() // suspend call
                    } catch (t: Throwable) {
                        error = t
                    } finally {
                        loading = false
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)

                    when {
                        loading -> Text("Loading…")
                        error != null -> Text("Error: ${error?.message ?: "unknown"}")
                        greeting != null -> Text("Compose: $greeting")
                        else -> Text("Ready")
                    }
                }
            }
        }
    }
}