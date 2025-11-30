package nz.eloque.foss_wallet.model

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import org.json.JSONObject
import java.nio.charset.Charset
import java.util.Locale

data class BarCode(
    private val format: BarcodeFormat,
    private val message: String,
    private val encoding: Charset,
    val altText: String?,
) {

    fun toJson(): JSONObject {
        return JSONObject().also {
            it.put("format", format.toString())
            it.put("message", message)
            it.put("messageEncoding", encoding)
            it.put("altText", altText)
        }
    }

    fun is1d(): Boolean {
        return when (format) {
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
    }

    fun hasLegacyRepresentation() : Boolean {
        val legacyRepresentation = encodeAsBitmap(100, 100, true)
        val representation = encodeAsBitmap(100, 100, false)
        return !representation.sameAs(legacyRepresentation)
    }

    fun encodeAsBitmap(width: Int, height: Int, legacyRendering: Boolean): Bitmap {
        val encodeHints = mapOf(Pair(EncodeHintType.CHARACTER_SET, encoding))
        val result = MultiFormatWriter().encode(message, format, width, height, if (legacyRendering) null else encodeHints)
        val w = result.width
        val h = result.height
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            val offset = y * w
            for (x in 0 until w) {
                pixels[offset + x] = if (result[x, y]) Color.BLACK else Color.WHITE
            }
        }
        val bitmap = createBitmap(w, h)
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
        return bitmap
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

        fun fromJson(json: JSONObject): BarCode {
            return BarCode(
                formatFromString(json.getString("format")),
                json.getString("message"),
                Charset.forName(json.optString("messageEncoding", FALLBACK_CHARSET.toString())),
                if (json.has("altText")) { json.getString("altText") } else { null }
            )
        }

        fun formatFromString(format: String): BarcodeFormat {
            return when {
                format.contains("417") -> BarcodeFormat.PDF_417
                format.uppercase(Locale.ENGLISH).contains("AZTEC") -> return BarcodeFormat.AZTEC
                format.uppercase(Locale.ENGLISH).contains("128") -> return BarcodeFormat.CODE_128
                format.uppercase(Locale.ENGLISH).contains("39") -> return BarcodeFormat.CODE_39
                format.uppercase(Locale.ENGLISH).contains("93") -> return BarcodeFormat.CODE_93
                else -> BarcodeFormat.QR_CODE
            }
        }
    }

}