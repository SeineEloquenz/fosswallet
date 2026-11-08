package nz.eloque.foss_wallet.ui.screens.create

import android.graphics.Bitmap
import android.graphics.Point
import com.google.zxing.BarcodeFormat
import nz.eloque.foss_wallet.model.BarCode
import zxingcpp.BarcodeReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

enum class ScanSource {
    Image,
    Pdf,
}

object FileScanner {
    private val barcodeReader =
        BarcodeReader(
            BarcodeReader.Options(
                tryHarder = true,
                tryRotate = true,
                tryInvert = true,
                tryDenoise = true,
                tryDownscale = true,
                binarizer = BarcodeReader.Binarizer.GLOBAL_HISTOGRAM,
            ),
        )

    /** A decoded barcode. [unsupportedFormat] names the scanned format when [format] is a QR fallback for it. */
    data class ScanResult(
        val message: String,
        val format: BarcodeFormat,
        val encoding: Charset,
        val altText: String?,
        val unsupportedFormat: String? = null,
    ) {
        fun toBarCode(): BarCode =
            BarCode(
                format = format,
                message = message,
                encoding = encoding,
                altText = altText,
            )

        companion object {
            /**
             * Builds a scan result from the raw bytes, keeping zxing-cpp's display text only as caption for plain text.
             */
            fun fromDecoded(
                text: String?,
                bytes: ByteArray?,
                contentType: BarcodeReader.ContentType,
                format: BarcodeFormat,
            ): ScanResult? {
                val message = bytes?.let { String(it, StandardCharsets.ISO_8859_1) }?.takeIf { it.isNotBlank() } ?: return null
                val caption = text?.takeIf { contentType == BarcodeReader.ContentType.TEXT && it.isNotBlank() }
                return ScanResult(message, format, StandardCharsets.ISO_8859_1, caption)
            }
        }
    }

    /** A decoded barcode with its corners in the scanned bitmap's coordinates. */
    data class Detection(
        val result: ScanResult,
        val corners: List<Point>,
    )

    @Synchronized
    fun detect(bitmap: Bitmap): Detection? =
        barcodeReader.read(bitmap).firstNotNullOfOrNull { found ->
            found.toScanResult()?.let { result ->
                with(found.position) { Detection(result, listOf(topLeft, topRight, bottomRight, bottomLeft)) }
            }
        }

    /** Converts a zxing-cpp result, falling back to QR for formats the app cannot render. */
    fun BarcodeReader.Result.toScanResult(): ScanResult? {
        val renderable = BarCode.formatFromString(format.name)
        return ScanResult
            .fromDecoded(text, bytes, contentType, renderable ?: BarcodeFormat.QR_CODE)
            ?.copy(unsupportedFormat = format.name.takeIf { renderable == null })
    }
}
