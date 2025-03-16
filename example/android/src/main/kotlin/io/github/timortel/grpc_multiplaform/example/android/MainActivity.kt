package io.github.timortel.grpc_multiplaform.example.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import io.github.timortel.grpc_multiplatform.example.KMHelloServiceStub
import io.github.timortel.grpc_multiplatform.example.kmHelloRequest
import io.github.timortel.kotlin_multiplatform_grpc_lib.KMChannel

class MainActivity : AppCompatActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val scope = rememberCoroutineScope()
            var hostName by remember { mutableStateOf("") }
            var port by remember { mutableStateOf("") }

            var greet by remember { mutableStateOf("") }
            var resp by remember { mutableStateOf("") }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = { TopAppBar(title = { Text(text = "gRPC Example") }) }
            ) { padding ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TextField(
                        value = hostName,
                        onValueChange = { hostName = it },
                        label = { Text("Server host name") }
                    )

                    TextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("Server port") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    TextField(
                        value = greet,
                        onValueChange = { greet = it },
                        label = { Text("Enter your greeting") }
                    )

                    Button(
                        content = {
                            Text(text = "Click to perform GRPC request")
                        },
                        onClick = {
                            scope.launch {
                                val channel =
                                    KMChannel.Builder.forAddress(hostName, port.toIntOrNull() ?: return@launch)
                                        .usePlaintext()
                                        .build()

                                val stub = KMHelloServiceStub(channel)
                                resp = try {
                                    stub.sayHello(
                                        request = kmHelloRequest {
                                            greeting = greet
                                        }
                                    ).response
                                } catch (e: Exception) {
                                    e.message ?: "An error occurred"
                                }
                            }
                        },
                        enabled = hostName.isNotBlank() && port.toIntOrNull() != null && greet.isNotBlank()
                    )

                    Text(text = "Response from server: $resp")
                }
            }
        }
    }
}
