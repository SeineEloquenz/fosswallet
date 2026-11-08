package nz.eloque.foss_wallet.ui.screens.scan

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.google.zxing.BarcodeFormat
import nz.eloque.foss_wallet.model.BarCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
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

    @Test
    fun `file preview opens the selected PDF with a read grant`() {
        val uri = Uri.parse("content://sender/123")
        val intent = FilePreviewContract().createIntent(RuntimeEnvironment.getApplication(), FilePreviewInput(uri, "application/pdf"))
        assertEquals(uri, intent.data)
        assertEquals("application/pdf", intent.type)
        assertEquals("nz.eloque.foss_wallet.ui.screens.create.ScanActivity", intent.component!!.className)
        assertTrue(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
    }

    @Test
    fun `binary barcode survives the file preview result`() {
        val barcode = BarCode(BarcodeFormat.AZTEC, "#UT01x\u009c\u0000\r\u001a5", Charsets.ISO_8859_1, null)
        assertEquals(barcode, FilePreviewContract().parseResult(Activity.RESULT_OK, ScanContract.resultIntent(barcode)))
        assertNull(FilePreviewContract().parseResult(Activity.RESULT_CANCELED, ScanContract.resultIntent(barcode)))
    }

    private fun assertRoundTrip(barcode: BarCode) {
        assertEquals(barcode, contract.parseResult(Activity.RESULT_OK, ScanContract.resultIntent(barcode)))
    }
}
