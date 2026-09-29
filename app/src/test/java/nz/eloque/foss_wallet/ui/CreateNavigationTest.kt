package nz.eloque.foss_wallet.ui

import android.app.Activity
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import androidx.navigation.toRoute
import com.google.zxing.BarcodeFormat
import nz.eloque.foss_wallet.model.BarCode
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.nio.charset.Charset

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CreateNavigationTest {
    @Test
    fun `multiline barcode opens editor without changing its contents`() {
        assertBarcodeRoundTrip("A\r\nB", "A\r\nB")
    }

    @Test
    fun `reserved characters and Unicode survive navigation`() {
        assertBarcodeRoundTrip("https://example.org/a?x=1&y=2#fragment + %20 / Zürich", "?message=other&altText=other# + %20")
    }

    @Test
    fun `missing alternate text remains null`() {
        assertBarcodeRoundTrip("example", null)
    }

    @Test
    fun `empty alternate text remains empty`() {
        assertBarcodeRoundTrip("example", "")
    }

    @Test
    fun `barcode format and non-default encoding survive navigation`() {
        assertBarcodeRoundTrip("Zürich", "Ticket", BarcodeFormat.AZTEC, Charsets.ISO_8859_1)
    }

    private fun assertBarcodeRoundTrip(
        message: String,
        altText: String?,
        format: BarcodeFormat = BarcodeFormat.QR_CODE,
        encoding: Charset = Charsets.UTF_8,
    ) {
        Robolectric.buildActivity(Activity::class.java).setup().use { activity ->
            val controller = NavHostController(activity.get())
            controller.navigatorProvider.addNavigator(ComposeNavigator())
            controller.graph =
                controller.createGraph(startDestination = "wallet") {
                    composable("wallet") {}
                    composable<CreateWithBarcode> {}
                }
            val barcode = BarCode(format, message, encoding, altText)

            Screen.Create.navigate(controller, barcode)

            assertEquals(barcode, controller.currentBackStackEntry!!.toRoute<CreateWithBarcode>().toBarCode())
        }
    }
}
