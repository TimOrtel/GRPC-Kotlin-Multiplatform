package io.github.timortel.kmpgrpc.composeexample.composeappjs

import androidx.compose.runtime.*
import io.github.timortel.kmpgrpc.composeexample.shared.Communication
import io.github.timortel.kmpgrpc.composeexample.shared.numMessage
import io.github.timortel.kmpgrpc.core.Channel
import io.github.timortel.kmpgrpc.core.StatusException
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable
import kotlinx.coroutines.*

const val TAG_BUTTON_BACK = "TAG_BUTTON_BACK"
const val TAG_HOST = "TAG_HOST"
const val TAG_PORT = "TAG_PORT"

const val TAG_UNARY_RPC = "TAG_UNARY_RPC"
const val TAG_SERVER_STREAMING_RPC = "TAG_SERVER_STREAMING_RPC"

const val TAG_SERVER_RESPONSE = "TAG_SERVER_RESPONSE"

fun main() {
    renderComposable("root") {
        App()
    }
}

@Composable
fun App() {
    var currentDestination: Destination by remember { mutableStateOf(Destination.Home) }
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("") }
    val parsedPort = port.toIntOrNull() ?: 0

    Div({ style { padding(16.px) } }) {
        when (currentDestination) {
            Destination.Home -> HomeDestination(
                host = host,
                port = port,
                onUpdateHost = {
                    host = it
                },
                onUpdatePort = { port = it }
            ) {
                currentDestination = it
            }

            Destination.Unary -> {
                UnaryDestination(host = host, port = parsedPort) {
                    currentDestination = Destination.Home
                }
            }

            Destination.ServerStreaming -> {
                ServerStreamingDestination(host = host, port = parsedPort) {
                    currentDestination = Destination.Home
                }
            }
        }
    }
}

sealed class Destination(val name: String, val description: String, val buttonTag: String) {
    object Home :
        Destination("Home", "Select which of the following scenarios to explore.", "")

    object Unary :
        Destination("Unary Call", "Send a number to the server and get the square.", TAG_UNARY_RPC)

    object ServerStreaming :
        Destination("Server streaming", "Send a number. Server will count down.", TAG_SERVER_STREAMING_RPC)
}

@Composable
fun HomeDestination(
    host: String,
    port: String,
    onUpdateHost: (String) -> Unit,
    onUpdatePort: (String) -> Unit,
    onChangeDestination: (Destination) -> Unit
) {
    DestinationBase(destination = Destination.Home, onNavigateBack = null) {
        Label(forId = "host") { Text("Host:") }

        Input(InputType.Text) {
            id(TAG_HOST)
            value(host)
            onInput { onUpdateHost(it.value) }
        }
        Br()
        Label(forId = "port") { Text("Port:") }
        Input(InputType.Text) {
            id(TAG_PORT)
            value(port)
            onInput { onUpdatePort(it.value) }
        }
        Br()
        listOf(
            Destination.Unary,
            Destination.ServerStreaming
        ).forEach { dest ->
            Button(attrs = {
                id(dest.buttonTag)
                if (host.isBlank() || port.toIntOrNull() == null) disabled()
                onClick {
                    onChangeDestination(dest)
                }
            }) {
                Text(dest.name)
            }
            Br()
        }
    }
}

@Composable
fun UnaryDestination(
    host: String,
    port: Int,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val stub = rememberCommunicationStub(host, port)

    var serverResponse: String by remember { mutableStateOf("No response") }

    DestinationWithNumPadBase(
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
            serverResponse = serverResponse
        )
    }
}

@Composable
fun ServerStreamingDestination(
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
        numPadEnabled = enteredNum == null,
        destination = Destination.ServerStreaming,
        onNavigateBack = onNavigateBack,
        onNumberEntered = { num ->
            enteredNum = num
        }
    ) {
        ServerResponse(
            serverResponse = serverResponse
        )
    }
}

@Composable
fun DestinationWithNumPadBase(
    destination: Destination,
    numPadEnabled: Boolean,
    onNumberEntered: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    content: @Composable () -> Unit
) {
    DestinationBase(destination = destination, onNavigateBack = onNavigateBack) {
        NumPad(
            enabled = numPadEnabled,
            onNumberEntered = onNumberEntered
        )

        content()
    }
}

@Composable
fun DestinationBase(
    destination: Destination,
    onNavigateBack: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Div(
        attrs = {
            style {
                padding(16.px)
                fontFamily("sans-serif")
            }
        }
    ) {
        Div(
            attrs = {
                style {
                    display(DisplayStyle.Flex)
                    justifyContent(JustifyContent.SpaceBetween)
                    alignItems(AlignItems.Center)
                }
            }
        ) {
            if (onNavigateBack != null) {
                Button(
                    attrs = {
                        onClick { onNavigateBack() }
                        id(TAG_BUTTON_BACK)
                    }
                ) {
                    Text("← Back")
                }
            }

            H2 { Text(destination.name) }
        }

        P {
            Text(destination.description)
        }

        content()
    }
}

@Composable
fun NumPad(enabled: Boolean, onNumberEntered: (Int) -> Unit) {
    Div(
        attrs = {
            style {
                display(DisplayStyle.Grid)
                gridTemplateColumns("repeat(3, 60px)")
                gap(8.px)
                justifyContent(JustifyContent.Center)
                marginBottom(16.px)
            }
        }
    ) {
        (1..9).forEach { num ->
            Button(
                attrs = {
                    if (!enabled) disabled()

                    id(getNumPadButtonId(num))

                    style {
                        width(60.px)
                        height(60.px)
                        fontSize(20.px)
                    }
                    onClick { onNumberEntered(num) }
                }
            ) {
                Text(num.toString())
            }
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun rememberCommunicationStub(host: String, port: Int): Communication.CommunicationServiceStub {
    val channel = remember {
        Channel.Builder.forAddress(host, port).usePlaintext().build()
    }

    DisposableEffect(channel) {
        onDispose {
            GlobalScope.launch {
                channel.shutdownNow()
            }
        }
    }

    return remember(channel) {
        Communication.CommunicationServiceStub(channel)
    }
}

@Composable
private fun ServerResponse(serverResponse: String) {
    P(
        attrs = {
            id(TAG_SERVER_RESPONSE)
            style {
                textAlign("center")
                whiteSpace("pre-wrap")
            }
        }
    ) {
        Text("Response from server:\n$serverResponse")
    }
}

fun getNumPadButtonId(num: Int) = "num$num"
