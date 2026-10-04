package nz.eloque.foss_wallet.ui.screens.pass

import androidx.activity.ComponentDialog
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.google.zxing.BarcodeFormat
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.persistence.BarcodePosition
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDialog

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BarcodesTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun fullscreenOpensSelectedBarcodeAndSwipesBothWays() {
        composeRule.setContent {
            Barcodes(
                barcodes = List(3) { barcode("Barcode $it") },
                legacyRendering = false,
                barcodePosition = BarcodePosition.Center,
                increaseFullscreenBrightness = false,
            )
        }
        composeRule.onNode(hasScrollToIndexAction()).performScrollToIndex(1)
        composeRule.onNodeWithContentDescription("Barcode").performClick()

        val pager = composeRule.onNode(hasScrollToIndexAction() and hasAnyAncestor(isDialog()))

        fun assertPage(expected: Int) {
            composeRule.waitForIdle()
            val range = pager.fetchSemanticsNode().config[SemanticsProperties.HorizontalScrollAxisRange]
            assertEquals(expected / 2f, range.value() / range.maxValue(), 0.01f)
        }

        assertPage(1)
        pager.performTouchInput { swipeLeft() }
        assertPage(2)
        pager.performTouchInput { swipeRight() }
        assertPage(1)
        pager.performTouchInput { swipeRight() }
        assertPage(0)
    }

    @Test
    fun dismissingFullscreenKeepsSwipedPage() {
        composeRule.setContent {
            Barcodes(
                barcodes = List(3) { barcode("Barcode $it") },
                legacyRendering = false,
                barcodePosition = BarcodePosition.Center,
                increaseFullscreenBrightness = false,
            )
        }
        composeRule.onNodeWithContentDescription("Barcode").performClick()
        composeRule.onNode(hasScrollToIndexAction() and hasAnyAncestor(isDialog())).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.runOnUiThread {
            (ShadowDialog.getLatestDialog() as ComponentDialog).onBackPressedDispatcher.onBackPressed()
        }
        composeRule.waitForIdle()

        composeRule.onNode(isDialog()).assertDoesNotExist()
        val range =
            composeRule
                .onNode(hasScrollToIndexAction())
                .fetchSemanticsNode()
                .config[SemanticsProperties.HorizontalScrollAxisRange]
        assertEquals(0.5f, range.value() / range.maxValue(), 0.01f)
    }

    @Test
    fun singleBarcodeOpensFullscreen() {
        composeRule.setContent {
            Barcodes(
                barcodes = listOf(barcode("Single barcode")),
                legacyRendering = false,
                barcodePosition = BarcodePosition.Center,
                increaseFullscreenBrightness = false,
            )
        }
        composeRule.onNodeWithContentDescription("Barcode").performClick()
        composeRule
            .onNode(hasContentDescription("Barcode") and hasAnyAncestor(isDialog()))
            .assertExists()
    }

    private fun barcode(message: String) = BarCode(BarcodeFormat.QR_CODE, message, Charsets.UTF_8, null)
}
