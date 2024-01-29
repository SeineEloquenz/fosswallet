package nz.eloque.foss_wallet.model

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import nz.eloque.foss_wallet.model.BarCode.Companion.TAG
import java.util.Locale
import java.util.UUID

class BarCode(val format: BarcodeFormat?, val message: String? = UUID.randomUUID().toString().uppercase(
    Locale.ROOT)) {

    var alternativeText: String? = null

    fun getBitmap(resources: Resources): BitmapDrawable? {
        if (message == null) {
            // no message -> no barcode
            return null
        }

        if (format == null) {
            return generateBitmapDrawable(resources, message, BarcodeFormat.QR_CODE)
        }

        return generateBitmapDrawable(resources, message, format)

    }

    companion object {

        const val TAG = "BarCode"

        fun getFormatFromString(format: String): BarcodeFormat {
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
fun generateBitmapDrawable(resources: Resources, data: String, type: BarcodeFormat): BitmapDrawable? {
    val bitmap = generateBarCodeBitmap(data, type) ?: return null

    return BitmapDrawable(resources, bitmap).apply {
        isFilterBitmap = false
        setAntiAlias(false)
    }
}

fun generateBarCodeBitmap(data: String, type: BarcodeFormat): Bitmap? {

    if (data.isEmpty()) {
        return null
    }

    try {
        val matrix = getBitMatrix(data, type)
        val is1D = matrix.height == 1

        // generate an image from the byte matrix
        val width = matrix.width
        val height = if (is1D) width / 5 else matrix.height

        // create buffered image to draw to
        // NTFS Bitmap.Config.ALPHA_8 sounds like an awesome idea - been there - done that ..
        val barcodeImage = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        // iterate through the matrix and draw the pixels to the image
        for (y in 0 until height) {
            for (x in 0 until width) {
                barcodeImage.setPixel(x, y, if (matrix.get(x, if (is1D) 0 else y)) 0 else 0xFFFFFF)
            }
        }

        return barcodeImage
    } catch (e: com.google.zxing.WriterException) {
        Log.e(TAG,"could not write image")
        // TODO check if we should better return some rescue Image here
        return null
    } catch (e: IllegalArgumentException) {
        Log.e(TAG, "could not write image: $e")
        return null
    } catch (e: ArrayIndexOutOfBoundsException) {
        // happens for ITF barcode on certain inputs
        Log.e(TAG, "could not write image: $e")
        return null
    }

}

fun getBitMatrix(data: String, type: BarcodeFormat) = MultiFormatWriter().encode(data, type, 0, 0)!!