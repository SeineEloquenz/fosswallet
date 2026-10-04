package nz.eloque.foss_wallet.ui.screens.create

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.core.graphics.createBitmap
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

    data class ScanResult(
        val message: String,
        val format: BarcodeFormat,
        val encoding: Charset,
        val altText: String?,
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

    fun scanFrom(
        contentResolver: ContentResolver,
        uri: Uri,
        scanSource: ScanSource,
    ): ScanResult? =
        when (scanSource) {
            ScanSource.Image -> scanFromImage(contentResolver, uri)
            ScanSource.Pdf -> scanFromPdf(contentResolver, uri)
        }

    fun scanFromPdf(
        contentResolver: ContentResolver,
        uri: Uri,
    ): ScanResult? =
        contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
            PdfRenderer(descriptor).use { renderer ->
                for (i in 0 until renderer.pageCount) {
                    renderer.openPage(i).use { page ->
                        val scale = 3f // Increase for better QR detection

                        val width = (page.width * scale).toInt()
                        val height = (page.height * scale).toInt()

                        val bitmap = createBitmap(width, height)

                        val matrix =
                            Matrix().apply {
                                postScale(scale, scale)
                            }

                        page.render(
                            bitmap,
                            null,
                            matrix,
                            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY,
                        )

                        val scanResult = scanFrom(bitmap)
                        if (scanResult != null) {
                            return scanResult
                        }
                    }
                }
            }

            return null
        }

    fun scanFromImage(
        contentResolver: ContentResolver,
        uri: Uri,
    ): ScanResult? {
        val source = ImageDecoder.createSource(contentResolver, uri)
        val bitmap =
            ImageDecoder.decodeBitmap(source) { decoder, info, source ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = false
            }
        return scanFrom(bitmap)
    }

    fun scanFrom(bitmap: Bitmap): ScanResult? = barcodeReader.read(bitmap).firstOrNull()?.toScanResult()

    /** Converts a zxing-cpp result, returning null for formats the app cannot render. */
    fun BarcodeReader.Result.toScanResult(): ScanResult? {
        val format = BarcodeFormat.entries.firstOrNull { it.name == format.name } ?: return null
        return ScanResult.fromDecoded(text, bytes, contentType, format)
    }
}
