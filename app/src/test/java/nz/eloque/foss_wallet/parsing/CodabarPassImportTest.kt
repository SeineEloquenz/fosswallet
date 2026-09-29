package nz.eloque.foss_wallet.parsing

import com.google.zxing.BarcodeFormat
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.persistence.loader.PassBitmaps
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.Mockito

@RunWith(Parameterized::class)
class CodabarPassImportTest(
    private val barcodeKey: String,
) {
    @Test
    fun `imports Codabar without replacing it with a QR code`() {
        val barcodeJson =
            JSONObject()
                .put("format", "PKBarcodeFormatCodabar")
                .put("message", "A2901001234567A")
                .put("messageEncoding", "iso-8859-1")
                .put("altText", "2901001234567")
        val passJson =
            JSONObject()
                .put("formatVersion", 1)
                .put("description", "Library card")
                .put("organizationName", "Example library")
                .put("serialNumber", "codabar-test")
                .put(barcodeKey, if (barcodeKey == "barcodes") JSONArray().put(barcodeJson) else barcodeJson)

        val pass = PassParser().parse(passJson, bitmaps = Mockito.mock(PassBitmaps::class.java))
        val barcode = pass.barCodes.single()

        assertEquals(
            BarCode(BarcodeFormat.CODABAR, "A2901001234567A", Charsets.ISO_8859_1, "2901001234567"),
            barcode,
        )
        assertFalse(barcode.isNotValid())
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): List<Array<String>> = listOf(arrayOf("barcodes"), arrayOf("barcode"))
    }
}
