package io.github.timortel.kmpgrpc.example.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.timortel.kmpgrpc.example.common.GreetingLogic
import kotlinx.coroutines.launch

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
                        modifier = Modifier.fillMaxWidth(),
                        value = hostName,
                        onValueChange = { hostName = it },
                        label = { Text("Server host name") }
                    )

                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("Server port") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = greet,
                        onValueChange = { greet = it },
                        label = { Text("Enter your greeting") }
                    )

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        content = {
                            Text(text = "Click to perform GRPC request")
                        },
                        onClick = {
                            scope.launch {
                                resp =
                                    GreetingLogic.performGreeting(hostName, port.toIntOrNull() ?: return@launch, greet)
                            }
                        },
                        enabled = hostName.isNotBlank() && port.toIntOrNull() != null && greet.isNotBlank()
                    )

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Response from server: $resp"
                    )
                }
            }
        }
    }
}
