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

    fun is1d(): Boolean =
        when (format) {
            BarcodeFormat.AZTEC -> false
            BarcodeFormat.CODABAR -> true
            BarcodeFormat.CODE_39 -> true
            BarcodeFormat.CODE_93 -> true
            BarcodeFormat.CODE_128 -> true
            BarcodeFormat.DATA_MATRIX -> false
            BarcodeFormat.EAN_8 -> true
            BarcodeFormat.EAN_13 -> true
            BarcodeFormat.ITF -> true
            BarcodeFormat.MAXICODE -> false
            BarcodeFormat.PDF_417 -> true
            BarcodeFormat.QR_CODE -> false
            BarcodeFormat.RSS_14 -> true
            BarcodeFormat.RSS_EXPANDED -> true
            BarcodeFormat.UPC_A -> true
            BarcodeFormat.UPC_E -> true
            BarcodeFormat.UPC_EAN_EXTENSION -> true
        }

    fun hasLegacyRepresentation(): Boolean {
        val legacyRepresentation = encode(legacyRendering = true) ?: return false
        val representation = encode(legacyRendering = false) ?: return false

        return representation != legacyRepresentation
    }

    fun isNotValid() = encode() == null

    private fun encode(
        width: Int = 0,
        height: Int = 0,
        legacyRendering: Boolean = false,
    ): BitMatrix? {
        val encodeHints = mapOf(Pair(EncodeHintType.CHARACTER_SET, encoding))

        return try {
            MultiFormatWriter().encode(message, format, width, height, if (legacyRendering) null else encodeHints)
        } catch (_: Exception) {
            null
        }
    }

    fun toBitmap(
        width: Int = 0,
        height: Int = 0,
        legacyRendering: Boolean = false,
    ): Bitmap? {
        val bitMatrix = encode(width, height, legacyRendering) ?: return null

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
        return altText == other.altText
    }

    override fun hashCode(): Int {
        var result = format.hashCode()
        result = 31 * result + message.hashCode()
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

        fun formatFromString(format: String): BarcodeFormat =
            when (format) {
                "PKBarcodeFormatPDF417" -> BarcodeFormat.PDF_417
                "PKBarcodeFormatAztec" -> BarcodeFormat.AZTEC
                "PKBarcodeFormatCode128" -> BarcodeFormat.CODE_128
                "PKBarcodeFormatCode39" -> BarcodeFormat.CODE_39
                "PKBarcodeFormatCode93" -> BarcodeFormat.CODE_93
                else -> BarcodeFormat.QR_CODE
            }
    }
}
