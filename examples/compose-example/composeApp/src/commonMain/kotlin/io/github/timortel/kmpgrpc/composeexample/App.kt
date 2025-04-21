package io.github.timortel.kmpgrpc.composeexample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import io.github.timortel.kmpgrpc.composeexample.shared.Communication
import io.github.timortel.kmpgrpc.composeexample.shared.numMessage
import io.github.timortel.kmpgrpc.core.Channel
import io.github.timortel.kmpgrpc.core.StatusException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch

const val TAG_BUTTON_BACK = "TAG_BUTTON_BACK"

const val TAG_HOST = "TAG_HOST"
const val TAG_PORT = "TAG_PORT"

const val TAG_UNARY_RPC = "TAG_UNARY_RPC"
const val TAG_CLIENT_STREAMING_RPC = "TAG_CLIENT_STREAMING_RPC"
const val TAG_SERVER_STREAMING_RPC = "TAG_SERVER_STREAMING_RPC"
const val TAG_BIDI_STREAMING_RPC = "TAG_BIDI_STREAMING_RPC"

const val TAG_SERVER_RESPONSE = "TAG_SERVER_RESPONSE"

const val TAG_OPEN_CLOSE_SENDING_FLOW = "TAG_OPEN_CLOSE_SENDING_FLOW"

private sealed class Destination(val name: String, val description: String, val buttonTag: String) {
    data object Home : Destination("Home", "Select which of the following scenarios to explore.", "")

    data object Unary : Destination("Unary Call", "Send a number to the server and get the square.", TAG_UNARY_RPC)
    data object ClientStreaming : Destination(
        "Client streaming",
        "Calculate the average of the numbers entered. Send multiple numbers to the server. Close the sending flow to get the response from the server.",
        TAG_CLIENT_STREAMING_RPC
    )

    data object ServerStreaming :
        Destination(
            "Server streaming",
            "Send a number to the server. The server will respond with a countdown to 0.",
            TAG_SERVER_STREAMING_RPC
        )

    data object BidiStreaming : Destination(
        "Bidi streaming",
        "Send multiple numbers to the server. After each number, the server will send the latest average.",
        TAG_BIDI_STREAMING_RPC
    )
}

@Composable
fun App(modifier: Modifier) {
    var currentDestination: Destination by remember { mutableStateOf(Destination.Home) }

    var host: String by remember { mutableStateOf("") }
    var port: String by remember { mutableStateOf("") }

    val destinationModifier = Modifier.fillMaxSize()

    val parsedPort = port.toIntOrNull() ?: 0

    Box(modifier = modifier) {
        MaterialTheme {
            when (currentDestination) {
                Destination.Home -> HomeDestination(
                    modifier = destinationModifier,
                    host = host,
                    port = port,
                    onUpdateHost = { host = it },
                    onUpdatePort = { port = it },
                    onChangeDestination = { currentDestination = it }
                )

                Destination.Unary -> {
                    UnaryDestination(
                        modifier = destinationModifier,
                        host = host,
                        port = parsedPort,
                        onNavigateBack = {
                            currentDestination = Destination.Home
                        }
                    )
                }

                Destination.ClientStreaming -> {
                    ClientStreamingDestination(
                        modifier = destinationModifier,
                        host = host,
                        port = parsedPort,
                        onNavigateBack = {
                            currentDestination = Destination.Home
                        }
                    )
                }

                Destination.ServerStreaming -> {
                    ServerStreamingDestination(
                        modifier = destinationModifier,
                        host = host,
                        port = parsedPort,
                        onNavigateBack = {
                            currentDestination = Destination.Home
                        }
                    )
                }

                Destination.BidiStreaming -> {
                    BidiStreamingDestination(
                        modifier = destinationModifier,
                        host = host,
                        port = parsedPort,
                        onNavigateBack = {
                            currentDestination = Destination.Home
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeDestination(
    modifier: Modifier,
    host: String,
    port: String,
    onUpdateHost: (String) -> Unit,
    onUpdatePort: (String) -> Unit,
    onChangeDestination: (Destination) -> Unit
) {
    DestinationBase(modifier = modifier, destination = Destination.Home, onNavigateBack = null) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "First, enter the address of your server:"
            )

            TextField(
                modifier = Modifier.fillMaxWidth().testTag(TAG_HOST),
                value = host,
                label = { Text("host") },
                onValueChange = onUpdateHost
            )

            TextField(
                modifier = Modifier.fillMaxWidth().testTag(TAG_PORT),
                value = port,
                label = { Text("port") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                onValueChange = onUpdatePort
            )

            listOf(
                Destination.Unary,
                Destination.ClientStreaming,
                Destination.ServerStreaming,
                Destination.BidiStreaming
            ).fastForEach { dest ->
                Button(
                    modifier = Modifier.fillMaxWidth().testTag(dest.buttonTag),
                    onClick = {
                        onChangeDestination(dest)
                    },
                    enabled = host.isNotBlank() && port.toIntOrNull() != null
                ) {
                    Text(text = dest.name)
                }
            }
        }
    }
}

@Composable
private fun UnaryDestination(modifier: Modifier, host: String, port: Int, onNavigateBack: () -> Unit) {
    val scope = rememberCoroutineScope()

    val stub = rememberCommunicationStub(host, port)

    var serverResponse: String by remember { mutableStateOf("No response") }

    DestinationWithNumPadBase(
        modifier = modifier,
        destination = Destination.Unary,
        numPadEnabled = true,
        onNavigateBack = onNavigateBack,
        onNumberEntered = { num ->
            scope.launch {
                serverResponse = try {
                    stub.squareNumber(
                        numMessage { value = num }
                    ).value.toString()
                } catch (e: StatusException) {
                    e.status.toString()
                }
            }
        }
    ) {
        ServerResponse(
            modifier = Modifier.fillMaxWidth(),
            serverResponse = serverResponse
        )
    }
}

@Composable
private fun ClientStreamingDestination(modifier: Modifier, host: String, port: Int, onNavigateBack: () -> Unit) {
    val scope = rememberCoroutineScope()

    val stub = rememberCommunicationStub(host, port)

    var serverResponse: String by remember { mutableStateOf("No response") }

    val sendFlow = remember { MutableSharedFlow<Int?>() }

    var isSendingStreamOpen by remember { mutableStateOf(true) }

    LaunchedEffect(stub) {
        while (true) {
            // Wait for ready
            snapshotFlow { isSendingStreamOpen }.first { it }

            serverResponse = try {
                stub.finalAverage(
                    requests = sendFlow.takeWhile { it != null }.map { numMessage { value = it } }
                ).value.toString()
            } catch (e: StatusException) {
                e.printStackTrace()
                e.status.toString()
            }
        }
    }

    DestinationWithNumPadBase(
        modifier = modifier,
        numPadEnabled = isSendingStreamOpen,
        destination = Destination.ClientStreaming,
        onNavigateBack = onNavigateBack,
        onNumberEntered = { num ->
            scope.launch {
                sendFlow.emit(num)
            }
        }
    ) {
        Button(
            modifier = Modifier.fillMaxWidth().testTag(TAG_OPEN_CLOSE_SENDING_FLOW),
            onClick = {
                if (isSendingStreamOpen) {
                    scope.launch {
                        sendFlow.emit(null)
                    }
                } else {
                    serverResponse = "No response"
                }

                isSendingStreamOpen = !isSendingStreamOpen
            }
        ) {
            Text(
                if (isSendingStreamOpen) {
                    "Close sending flow"
                } else {
                    "Open sending flow"
                }
            )
        }

        ServerResponse(
            modifier = Modifier.fillMaxWidth(),
            serverResponse = serverResponse
        )
    }
}

@Composable
private fun ServerStreamingDestination(
    modifier: Modifier,
    host: String,
    port: Int,
    onNavigateBack: () -> Unit
) {
    val stub = rememberCommunicationStub(host, port)

    var serverResponse: String by remember { mutableStateOf("No response") }
    var enteredNum: Int? by remember { mutableStateOf(null) }

    LaunchedEffect(enteredNum) {
        if (enteredNum != null) {
            try {
                stub.countdown(numMessage { value = enteredNum })
                    .collect {
                        serverResponse = it.value.toString()
                    }

                enteredNum = null
                serverResponse = "No response"
            } catch (e: StatusException) {
                serverResponse = e.status.toString()
            }
        }
    }

    DestinationWithNumPadBase(
        modifier = modifier,
        numPadEnabled = enteredNum == null,
        destination = Destination.ServerStreaming,
        onNavigateBack = onNavigateBack,
        onNumberEntered = { num ->
            enteredNum = num
        }
    ) {
        ServerResponse(
            modifier = Modifier.fillMaxWidth(),
            serverResponse = serverResponse
        )
    }
}

@Composable
private fun BidiStreamingDestination(modifier: Modifier, host: String, port: Int, onNavigateBack: () -> Unit) {
    val scope = rememberCoroutineScope()

    val stub = rememberCommunicationStub(host, port)

    var serverResponse: String by remember { mutableStateOf("No response") }
    var enteredNum: Int? by remember { mutableStateOf(null) }

    val sendFlow = remember { MutableSharedFlow<Int?>() }

    LaunchedEffect(stub) {
        while (true) {
            try {
                stub.runningAverage(
                    requests = sendFlow.takeWhile { it != null }.map { numMessage { value = it } }
                )
                    .map { it.value }
                    .collect { serverResponse = it.toString() }
            } catch (e: StatusException) {
                e.status.toString()
            }

            serverResponse = "No response"
        }
    }

    DestinationWithNumPadBase(
        modifier = modifier,
        numPadEnabled = enteredNum == null,
        destination = Destination.BidiStreaming,
        onNavigateBack = onNavigateBack,
        onNumberEntered = { num ->
            scope.launch {
                sendFlow.emit(num)
            }
        }
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch {
                    sendFlow.emit(null)
                }
            }
        ) {
            Text("Restart")
        }

        ServerResponse(
            modifier = Modifier.fillMaxWidth(),
            serverResponse = serverResponse
        )
    }
}

@Composable
private fun DestinationBase(
    modifier: Modifier,
    destination: Destination,
    onNavigateBack: (() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = destination.name) },
                navigationIcon = onNavigateBack?.let {
                    {
                        IconButton(
                            modifier = Modifier.testTag(TAG_BUTTON_BACK),
                            onClick = it
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                null
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(modifier = Modifier.fillMaxWidth(), text = destination.description)

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }
}

@Composable
private fun DestinationWithNumPadBase(
    modifier: Modifier,
    destination: Destination,
    numPadEnabled: Boolean,
    onNumberEntered: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    DestinationBase(modifier = modifier, destination = destination, onNavigateBack = onNavigateBack) {
        NumPad(
            modifier = Modifier.fillMaxWidth(0.6f).align(Alignment.CenterHorizontally),
            enabled = numPadEnabled,
            onNumberEntered = onNumberEntered
        )

        Spacer(modifier = Modifier.height(16.dp))

        content()
    }
}

@Composable
private fun ServerResponse(modifier: Modifier, serverResponse: String) {
    Text(
        modifier = modifier.testTag(TAG_SERVER_RESPONSE),
        textAlign = TextAlign.Center,
        text = "Response from server:\n$serverResponse"
    )
}

@Composable
private fun NumPad(modifier: Modifier, enabled: Boolean, onNumberEntered: (Int) -> Unit) {
    BoxWithConstraints(modifier = modifier) {
        val size = maxWidth / 3

        Column(modifier = Modifier.fillMaxWidth()) {
            repeat(3) { i ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(3) { j ->
                        val num = (i * 3) + j + 1

                        Button(
                            modifier = Modifier.size(size).padding(4.dp),
                            enabled = enabled,
                            onClick = { onNumberEntered(num) }
                        ) {
                            Text(
                                fontSize = 20.sp,
                                text = num.toString(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun rememberCommunicationStub(host: String, port: Int): Communication.CommunicationServiceStub {
    val channel = remember {
        Channel.Builder.forAddress(host, port).usePlaintext().build()
    }

    DisposableEffect(channel) {
        onDispose {
            channel.shutdownNow()
        }
    }

    return remember(channel) {
        Communication.CommunicationServiceStub(channel)
    }
}
