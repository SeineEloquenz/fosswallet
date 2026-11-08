package nz.eloque.foss_wallet.model

import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import org.json.JSONObject
import java.nio.charset.Charset

data class BarCode(
    val format: BarcodeFormat,
    val message: String,
    val encoding: Charset,
    val altText: String?,
) {
    fun toJson(): JSONObject =
        JSONObject().also {
            it.put("format", format.toString())
            it.put("message", message)
            it.put("messageEncoding", encoding)
            it.put("altText", altText)
        }

    fun isNotValid() = encode() == null

    /** Encodes [message] as [encoding] bytes without an ECI segment. */
    internal fun encode(
        width: Int = 0,
        height: Int = 0,
    ): BitMatrix? {
        // A CHARACTER_SET hint makes zxing insert an ECI segment, which ticket and boarding scanners reject (#184, #312).
        val bytesAsLatin1 = String(message.toByteArray(encoding), Charsets.ISO_8859_1)
        return try {
            MultiFormatWriter().encode(bytesAsLatin1, format, width, height, mapOf(EncodeHintType.MARGIN to 0))
        } catch (_: Exception) {
            null
        }
    }

    fun toBitmap(
        width: Int = 0,
        height: Int = 0,
    ): Bitmap? {
        val bitMatrix = encode(width, height) ?: return null

        val w = bitMatrix.width
        val h = bitMatrix.height
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            val offset = y * w
            for (x in 0 until w) {
                pixels[offset + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }

        return createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BarCode
        if (format != other.format) return false
        if (message != other.message) return false
        if (encoding != other.encoding) return false
        return altText == other.altText
    }

    override fun hashCode(): Int {
        var result = format.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + encoding.hashCode()
        result = 31 * result + (altText?.hashCode() ?: 0)
        return result
    }

    companion object {
        val FALLBACK_CHARSET = Charsets.UTF_8

        fun fromJson(json: JSONObject): BarCode =
            BarCode(
                BarcodeFormat.valueOf(json.getString("format")),
                json.getString("message"),
                Charset.forName(json.optString("messageEncoding", FALLBACK_CHARSET.toString())),
                if (json.has("altText")) {
                    json.getString("altText")
                } else {
                    null
                },
            )

        private val renderableFormats =
            mapOf(
                "PKBarcodeFormatQR" to BarcodeFormat.QR_CODE,
                "PKBarcodeFormatPDF417" to BarcodeFormat.PDF_417,
                "PKBarcodeFormatAztec" to BarcodeFormat.AZTEC,
                "PKBarcodeFormatCode128" to BarcodeFormat.CODE_128,
                "PKBarcodeFormatCode39" to BarcodeFormat.CODE_39,
                "PKBarcodeFormatCode93" to BarcodeFormat.CODE_93,
                "PKBarcodeFormatCodabar" to BarcodeFormat.CODABAR,
                "PKBarcodeFormatDataMatrix" to BarcodeFormat.DATA_MATRIX,
                "PKBarcodeFormatEAN8" to BarcodeFormat.EAN_8,
                "PKBarcodeFormatEAN13" to BarcodeFormat.EAN_13,
                "PKBarcodeFormatI2of5" to BarcodeFormat.ITF,
                "PKBarcodeFormatITF" to BarcodeFormat.ITF,
                "PKBarcodeFormatUPCA" to BarcodeFormat.UPC_A,
                "PKBarcodeFormatUPCE" to BarcodeFormat.UPC_E,
            )

        /** Maps a pass.json or zxing format name to a format the app can render, or null if there is none. */
        fun formatFromString(format: String): BarcodeFormat? =
            renderableFormats[format] ?: renderableFormats.values.firstOrNull { it.name == format }
    }
}
