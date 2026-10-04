package nz.eloque.foss_wallet.ui.screens.scan

import android.app.Activity
import com.google.zxing.BarcodeFormat
import nz.eloque.foss_wallet.model.BarCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ScanContractTest {
    private val contract = ScanContract()

    @Test
    fun `binary barcode survives the scan result`() {
        assertRoundTrip(BarCode(BarcodeFormat.AZTEC, "#UT01x\u009c\u0000\r\u001a5", Charsets.ISO_8859_1, null))
    }

    @Test
    fun `text barcode keeps its caption`() {
        assertRoundTrip(BarCode(BarcodeFormat.QR_CODE, "Zürich HB", Charsets.ISO_8859_1, "Zürich HB"))
    }

    @Test
    fun `cancelled scan returns no barcode`() {
        val barcode = BarCode(BarcodeFormat.QR_CODE, "x", Charsets.UTF_8, null)

        assertNull(contract.parseResult(Activity.RESULT_CANCELED, ScanContract.resultIntent(barcode)))
        assertNull(contract.parseResult(Activity.RESULT_OK, null))
    }

    private fun assertRoundTrip(barcode: BarCode) {
        assertEquals(barcode, contract.parseResult(Activity.RESULT_OK, ScanContract.resultIntent(barcode)))
    }
}
