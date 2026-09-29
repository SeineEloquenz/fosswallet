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
class BarcodePassImportTest(
    private val barcodeKey: String,
    private val formatName: String,
    private val expectedFormat: BarcodeFormat,
    private val message: String,
) {
    @Test
    fun `preserves the imported barcode format and contents`() {
        val barcodeJson =
            JSONObject()
                .put("format", formatName)
                .put("message", message)
                .put("messageEncoding", "iso-8859-1")
                .put("altText", "2901001234567")
        val passJson =
            JSONObject()
                .put("formatVersion", 1)
                .put("description", "Example card")
                .put("organizationName", "Example library")
                .put("serialNumber", "barcode-test")
                .put(barcodeKey, if (barcodeKey == "barcodes") JSONArray().put(barcodeJson) else barcodeJson)

        val pass = PassParser().parse(passJson, bitmaps = Mockito.mock(PassBitmaps::class.java))
        val barcode = pass.barCodes.single()

        assertEquals(
            BarCode(expectedFormat, message, Charsets.ISO_8859_1, "2901001234567"),
            barcode,
        )
        assertFalse(barcode.isNotValid())
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}: {1}")
        fun data(): List<Array<Any>> {
            val formats =
                listOf(
                    Triple("PKBarcodeFormatQR", BarcodeFormat.QR_CODE, "Library card"),
                    Triple("PKBarcodeFormatPDF417", BarcodeFormat.PDF_417, "Library card"),
                    Triple("PKBarcodeFormatAztec", BarcodeFormat.AZTEC, "Library card"),
                    Triple("PKBarcodeFormatCode128", BarcodeFormat.CODE_128, "LIBRARY123"),
                    Triple("PKBarcodeFormatCode39", BarcodeFormat.CODE_39, "LIBRARY123"),
                    Triple("PKBarcodeFormatCode93", BarcodeFormat.CODE_93, "LIBRARY123"),
                    Triple("PKBarcodeFormatCodabar", BarcodeFormat.CODABAR, "A2901001234567A"),
                    Triple("PKBarcodeFormatDataMatrix", BarcodeFormat.DATA_MATRIX, "Library card"),
                    Triple("PKBarcodeFormatEAN8", BarcodeFormat.EAN_8, "96385074"),
                    Triple("PKBarcodeFormatEAN13", BarcodeFormat.EAN_13, "5901234123457"),
                    Triple("PKBarcodeFormatITF", BarcodeFormat.ITF, "12345678"),
                    Triple("PKBarcodeFormatUPCA", BarcodeFormat.UPC_A, "042100005264"),
                    Triple("PKBarcodeFormatUPCE", BarcodeFormat.UPC_E, "05096893"),
                )
            return formats.flatMap { (passFormat, format, message) ->
                listOf(passFormat, format.name).flatMap { formatName ->
                    listOf("barcodes", "barcode").map { barcodeKey ->
                        arrayOf(barcodeKey, formatName, format, message)
                    }
                }
            }
        }
    }
}
