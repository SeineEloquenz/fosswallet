package nz.eloque.foss_wallet.parsing

import com.google.zxing.BarcodeFormat
import nz.eloque.foss_wallet.persistence.loader.PassBitmaps
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito

class BarcodeFallbackWarningTest {
    @Test
    fun `reports unknown formats while preserving the QR fallback and contents`() {
        val formats = mutableListOf<String>()
        val pass = parse("PKBarcodeFormatUnknown", formats::add)

        assertEquals(BarcodeFormat.QR_CODE, pass.barCodes.single().format)
        assertEquals("123456", pass.barCodes.single().message)
        assertEquals(listOf("PKBarcodeFormatUnknown"), formats)
    }

    @Test
    fun `recognized formats do not report a fallback`() {
        val formats = mutableListOf<String>()
        listOf("PKBarcodeFormatQR", "QR_CODE", "PKBarcodeFormatCode128", "CODE_128").forEach { parse(it, formats::add) }

        assertTrue(formats.isEmpty())
    }

    private fun parse(
        format: String,
        onFallback: (String) -> Unit,
    ) = PassParser(onBarcodeFormatFallback = onFallback).parse(
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
