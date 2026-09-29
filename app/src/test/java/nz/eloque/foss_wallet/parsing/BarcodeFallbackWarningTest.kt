package nz.eloque.foss_wallet.parsing

import android.os.Looper
import com.google.zxing.BarcodeFormat
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.persistence.loader.PassBitmaps
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast
import java.util.concurrent.Executors

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BarcodeFallbackWarningTest {
    @Test
    fun `warns when an unknown barcode format is replaced with QR`() {
        val pass = parse("PKBarcodeFormatUnknown")
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(BarcodeFormat.QR_CODE, pass.barCodes.single().format)
        assertEquals("123456", pass.barCodes.single().message)
        assertEquals(
            "Imported as QR: PKBarcodeFormatUnknown is unsupported. This may not scan correctly.",
            ShadowToast.getTextOfLatestToast(),
        )
    }

    @Test
    fun `background imports show the fallback warning on the main thread`() {
        val executor = Executors.newSingleThreadExecutor()
        try {
            val pass = executor.submit<Pass> { parse("Unknown") }.get()
            shadowOf(Looper.getMainLooper()).idle()

            assertEquals(BarcodeFormat.QR_CODE, pass.barCodes.single().format)
            assertEquals(
                "Imported as QR: Unknown is unsupported. This may not scan correctly.",
                ShadowToast.getTextOfLatestToast(),
            )
        } finally {
            executor.shutdown()
        }
    }

    @Test
    fun `recognized formats do not show a fallback warning`() {
        listOf("PKBarcodeFormatQR", "QR_CODE", "PKBarcodeFormatCode128", "CODE_128").forEach { parse(it) }
        shadowOf(Looper.getMainLooper()).idle()

        assertNull(ShadowToast.getLatestToast())
    }

    private fun parse(format: String) =
        PassParser(RuntimeEnvironment.getApplication()).parse(
            JSONObject()
                .put("formatVersion", 1)
                .put("description", "Example card")
                .put("organizationName", "Example library")
                .put("serialNumber", "warning-test")
                .put(
                    "barcodes",
                    JSONArray().put(JSONObject().put("format", format).put("message", "123456")),
                ),
            bitmaps = Mockito.mock(PassBitmaps::class.java),
        )
}
