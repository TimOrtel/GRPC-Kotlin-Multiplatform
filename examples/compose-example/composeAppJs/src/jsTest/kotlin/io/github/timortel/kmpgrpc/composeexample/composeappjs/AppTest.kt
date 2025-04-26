package io.github.timortel.kmpgrpc.composeexample.composeappjs

import kotlinx.browser.document
import org.jetbrains.compose.web.testutils.ComposeWebExperimentalTestsApi
import org.jetbrains.compose.web.testutils.TestScope
import org.jetbrains.compose.web.testutils.runTest
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import kotlin.test.Test
import kotlin.test.assertContains

@OptIn(ComposeWebExperimentalTestsApi::class)
class AppTest {

    @Test
    fun testServerStreamingRpc() = runTest {
        setupTest()

        enterServerAddressAndSelect(TAG_SERVER_STREAMING_RPC)

        findElementById(getNumPadButtonId(2)).click()

        (1..2).reversed().forEach { num ->
            println("TEST FOR $num")
            waitForChanges(TAG_SERVER_RESPONSE)
            assertContains(findElementById(TAG_SERVER_RESPONSE).textContent.orEmpty(), num.toString())
        }
    }

    // Commented out due to a bug that only lets the first test pass. The click handler on the NumPad is not properly updated.
//    @Test
//    fun testUnaryRpc() = runTest {
//        setupTest()
//
//        enterServerAddressAndSelect(TAG_UNARY_RPC)
//
//        findElementById(getNumPadButtonId(3)).click()
//
//        waitForChanges(TAG_SERVER_RESPONSE)
//
//        assertContains(findElementById(TAG_SERVER_RESPONSE).textContent.orEmpty(), "9")
//    }

    fun TestScope.setupTest() {
        composition {
            App()
        }
    }

    suspend fun TestScope.enterServerAddressAndSelect(destinationTag: String) {
        findElementById(TAG_HOST).performInput("localhost")
        findElementById(TAG_PORT).performInput("8082")

        waitForRecompositionComplete()

        findElementById(destinationTag).click()

        waitForRecompositionComplete()
    }

    fun findElementById(id: String): HTMLElement {
        return document.getElementById(id) as HTMLElement
    }

    fun HTMLElement.performInput(value: String) {
        (this as HTMLInputElement).apply {
            this.value = value
            dispatchEvent(Event("input"))
        }
    }
}
