package io.github.timortel.kmpgrpc.composeexample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
abstract class AppTest(private val supportsClientStreaming: Boolean = true, private val port: Int = 17888) {

    open fun runComposeTest(block: ComposeUiTest.() -> Unit) = runComposeUiTest { block() }

    @Test
    fun testUnaryRpc() = runComposeTest {
        setupTest()

        enterServerAddressAndSelect(TAG_UNARY_RPC)

        onNodeWithText("3").performClick()

        waitUntilExactlyOneExists(hasTestTag(TAG_SERVER_RESPONSE) and hasText("9", substring = true))

        navigateBack()
    }

    @Test
    fun testClientStreamingRpc() = runComposeTest {
        if (!supportsClientStreaming) return@runComposeTest

        setupTest()

        enterServerAddressAndSelect(TAG_CLIENT_STREAMING_RPC)

        onNodeWithText("1").performClick()
        onNodeWithText("9").performClick()

        onNodeWithTag(TAG_OPEN_CLOSE_SENDING_FLOW).performClick()

        waitUntilExactlyOneExists(hasTestTag(TAG_SERVER_RESPONSE) and hasText("5", substring = true))

        navigateBack()
    }

    @Test
    fun testServerStreamingRpc() = runComposeTest {
        setupTest()

        enterServerAddressAndSelect(TAG_SERVER_STREAMING_RPC)

        onNodeWithText("3").performClick()

        (1..3).reversed().forEach { num ->
            waitUntilExactlyOneExists(hasTestTag(TAG_SERVER_RESPONSE) and hasText(num.toString(), substring = true), 2000)
        }

        navigateBack()
    }

    @Test
    fun testBidiStreamingRpc() = runComposeTest {
        if (!supportsClientStreaming) return@runComposeTest

        setupTest()

        enterServerAddressAndSelect(TAG_BIDI_STREAMING_RPC)

        onNodeWithText("1").performClick()
        waitUntilExactlyOneExists(hasTestTag(TAG_SERVER_RESPONSE) and hasText("1", substring = true))
        onNodeWithText("3").performClick()
        waitUntilExactlyOneExists(hasTestTag(TAG_SERVER_RESPONSE) and hasText("2", substring = true))

        navigateBack()
    }

    private fun ComposeUiTest.setupTest() {
        setContent {
            App(modifier = Modifier.fillMaxSize())
        }
    }

    private fun ComposeUiTest.enterServerAddressAndSelect(destinationTag: String) {
        onNodeWithTag(TAG_HOST).performTextInput("localhost")
        onNodeWithTag(TAG_PORT).performTextInput("$port")

        onNodeWithTag(destinationTag).performScrollTo().performClick()
    }

    private fun ComposeUiTest.navigateBack() {
        onNodeWithTag(TAG_BUTTON_BACK).performClick()
    }
}
