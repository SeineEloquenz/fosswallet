package nz.eloque.foss_wallet.model

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import org.json.JSONObject
import java.util.Locale

data class BarCode(
    private val format: BarcodeFormat,
    private val message: String
) {

    fun toJson(): JSONObject {
        return JSONObject().also {
            it.put("format", format.toString())
            it.put("message", message)
        }
    }

    var alternativeText: String? = null

    fun encodeAsBitmap(width: Int, height: Int): Bitmap {
        val result = MultiFormatWriter().encode(message, format, width, height, null)
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
        return alternativeText == other.alternativeText
    }

    override fun hashCode(): Int {
        var result = format.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + (alternativeText?.hashCode() ?: 0)
        return result
    }

    companion object {

        fun fromJson(json: JSONObject): BarCode {
            return BarCode(formatFromString(json.getString("format")), json.getString("message"))
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