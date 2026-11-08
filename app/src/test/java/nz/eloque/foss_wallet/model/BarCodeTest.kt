package nz.eloque.foss_wallet.model

import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Result
import com.google.zxing.ResultMetadataType
import com.google.zxing.aztec.encoder.Encoder
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Random

class BarCodeTest {
    private val binaryTicket =
        "#UT010080000001" + Random(4).let { random -> (1..375).map { random.nextInt(256).toChar() }.joinToString("") }

    @Test
    fun `aztec codes carry the pass bytes without an ECI`() {
        val barcode = BarCode(BarcodeFormat.AZTEC, binaryTicket, Charsets.ISO_8859_1, null)

        val expected =
            Encoder
                .encode(
                    binaryTicket.toByteArray(Charsets.ISO_8859_1),
                    Encoder.DEFAULT_EC_PERCENT,
                    Encoder.DEFAULT_AZTEC_LAYERS,
                ).matrix
        assertEquals(expected, barcode.encode())
        assertEquals(binaryTicket, decode(barcode.encode()!!).text)
    }

    @Test
    fun `qr codes carry no ECI for latin-1 and utf-8 passes`() {
        listOf(
            BarCode(BarcodeFormat.QR_CODE, "M1DOE/JANE EABC123", charset("iso-8859-1"), null),
            BarCode(BarcodeFormat.QR_CODE, "Zürich → Genève", Charsets.UTF_8, null),
        ).forEach { barcode ->
            val result = decode(barcode.encode()!!)
            assertEquals(barcode.message, result.text)
            assertEquals("]Q1", result.resultMetadata[ResultMetadataType.SYMBOLOGY_IDENTIFIER])
        }
    }

    private fun decode(matrix: BitMatrix): Result {
        val scale = 4
        val quiet = 8
        val width = (matrix.width + 2 * quiet) * scale
        val height = (matrix.height + 2 * quiet) * scale
        val pixels = IntArray(width * height) { -1 }
        for (y in 0 until height) {
            for (x in 0 until width) {
                val mx = x / scale - quiet
                val my = y / scale - quiet
                if (mx in 0 until matrix.width && my in 0 until matrix.height && matrix[mx, my]) pixels[y * width + x] = 0xFF000000.toInt()
            }
        }
        val bitmap = BinaryBitmap(HybridBinarizer(RGBLuminanceSource(width, height, pixels)))
        return MultiFormatReader().decode(bitmap, mapOf(DecodeHintType.PURE_BARCODE to true))
    }
}
