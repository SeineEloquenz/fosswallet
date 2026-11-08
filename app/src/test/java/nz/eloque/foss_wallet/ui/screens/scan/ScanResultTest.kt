package nz.eloque.foss_wallet.ui.screens.scan

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.google.zxing.BarcodeFormat
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.ui.Route
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ScanResultTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `imported boarding barcode offers boarding pass creation and manual entry`() {
        val barcode =
            BarCode(
                BarcodeFormat.QR_CODE,
                "M1DOE/JANE            EEXAMPL1BUDLHRXY 00123284Y012A00001100",
                Charsets.UTF_8,
                null,
            )
        showResult(barcode)
        composeRule.onNodeWithText("Create Boarding Pass").assertExists()
        composeRule.onNodeWithText("Manual entry").performClick()
        composeRule.onNodeWithText("Manual editor").assertExists()
    }

    @Test
    fun `imported URL offers opening the website`() {
        showResult(BarCode(BarcodeFormat.QR_CODE, "https://example.org/ticket?a=1&b=2", Charsets.UTF_8, null))
        composeRule.onNodeWithText("Create Boarding Pass").assertDoesNotExist()
        composeRule.onNodeWithText("Open URL").assertExists()
    }

    @Test
    fun `ordinary barcode offers manual entry without boarding suggestion`() {
        showResult(BarCode(BarcodeFormat.CODE_128, "LOYALTY-12345", Charsets.UTF_8, null))
        composeRule.onNodeWithText("Create Boarding Pass").assertDoesNotExist()
        composeRule.onNodeWithText("Manual entry").assertExists()
    }

    private fun showResult(barcode: BarCode) {
        val viewModel = mock(ScanViewModel::class.java)
        composeRule.setContent {
            val navController = rememberNavController()
            NavHost(navController, startDestination = Route.ScanResult(barcode)) {
                composable<Route.ScanResult> { entry ->
                    val initialBarcode = entry.toRoute<Route.ScanResult>().toBarCode()
                    assertEquals(barcode, initialBarcode)
                    ScanView(navController, viewModel, initialBarcode)
                }
                composable<Route.Create> { entry ->
                    assertEquals(barcode, entry.toRoute<Route.Create>().toBarCode())
                    Text("Manual editor")
                }
            }
        }
    }
}
