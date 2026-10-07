package nz.eloque.foss_wallet.ui.screens.create

import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import nz.eloque.foss_wallet.model.BarCode
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import zxingcpp.BarcodeReader.ContentType
import java.nio.charset.StandardCharsets

class ScanResultTest {
    // UIC 918.3 style ticket, a short header followed by zlib compressed data
    private val ticketBytes = "2355543031789c2bc94cce4e2d5148492c495428a1800d00c8c11a35".hexToByteArray()

    // What zxing-cpp returns as text for ticketBytes in its default HRI text mode
    private val ticketText = "#UT01x<U+9C>+ÉLÎN-QHI,IT(¡<U+80><CR><NUL>ÈÁ<SUB>5"

    @Test
    fun `binary aztec ticket renders with its original bytes`() {
        val barcode = FileScanner.ScanResult.fromDecoded(ticketText, ticketBytes, ContentType.BINARY, BarcodeFormat.AZTEC)!!.toBarCode()

        assertArrayEquals(ticketBytes, barcode.renderedBytes())
        assertNull(barcode.altText)
    }

    @Test
    fun `iso 15434 data matrix renders with its original bytes`() {
        val bytes = "[)>\u001e06\u001dP12345\u001dQ7\u001e\u0004".toByteArray(StandardCharsets.ISO_8859_1)
        val text = "[)>␞06␝P12345␝Q7␞␄"

        val barcode = FileScanner.ScanResult.fromDecoded(text, bytes, ContentType.ISO15434, BarcodeFormat.DATA_MATRIX)!!.toBarCode()

        assertArrayEquals(bytes, barcode.renderedBytes())
        assertNull(barcode.altText)
    }

    @Test
    fun `utf-8 text qr code renders with its original bytes and keeps its text as caption`() {
        val text = "Zürich HB"
        val bytes = text.toByteArray(StandardCharsets.UTF_8)

        val barcode = FileScanner.ScanResult.fromDecoded(text, bytes, ContentType.TEXT, BarcodeFormat.QR_CODE)!!.toBarCode()

        assertArrayEquals(bytes, barcode.renderedBytes())
        assertEquals(text, barcode.altText)
    }

    @Test
    fun `scanned binary ticket keeps its bytes when saved from the create screen`() {
        val scanned = FileScanner.ScanResult.fromDecoded(ticketText, ticketBytes, ContentType.BINARY, BarcodeFormat.AZTEC)!!.toBarCode()

        val saved = BarcodeDraft.from(scanned).toBarCode()

        assertArrayEquals(ticketBytes, saved.renderedBytes())
        assertNull(saved.altText)
    }

    @Test
    fun `typed barcode uses its message as caption`() {
        val saved = BarcodeDraft(message = "123456", altText = "", format = BarcodeFormat.QR_CODE).toBarCode()

        assertEquals(StandardCharsets.UTF_8, saved.encoding)
        assertEquals("123456", saved.altText)
    }

    @Test
    fun `empty scans are ignored`() {
        assertNull(FileScanner.ScanResult.fromDecoded(" ", byteArrayOf(32), ContentType.TEXT, BarcodeFormat.QR_CODE))
        assertNull(FileScanner.ScanResult.fromDecoded("", ByteArray(0), ContentType.BINARY, BarcodeFormat.AZTEC))
    }

    private fun BarCode.renderedBytes(): ByteArray {
        val symbol = encode()!!
        val quietZone = 4
        val scale = 4
        val width = (symbol.width + 2 * quietZone) * scale
        val height = (symbol.height + 2 * quietZone) * scale
        val pixels =
            IntArray(width * height) { i ->
                val x = i % width / scale - quietZone
                val y = i / width / scale - quietZone
                if (x in 0 until symbol.width && y in 0 until symbol.height && symbol[x, y]) BLACK else WHITE
            }
        val image = BinaryBitmap(HybridBinarizer(RGBLuminanceSource(width, height, pixels)))
        val hints = mapOf(DecodeHintType.POSSIBLE_FORMATS to listOf(format), DecodeHintType.CHARACTER_SET to "ISO-8859-1")
        return MultiFormatReader().decode(image, hints).text.toByteArray(StandardCharsets.ISO_8859_1)
    }

    private companion object {
        const val BLACK = 0xFF000000.toInt()
        const val WHITE = 0xFFFFFFFF.toInt()
    }
}
