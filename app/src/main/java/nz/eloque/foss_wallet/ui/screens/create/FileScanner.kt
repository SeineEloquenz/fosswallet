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
        val text: String,
        val format: String,
    ) {
        fun toBarCode(): BarCode =
            BarCode(
                format = BarcodeFormat.valueOf(format),
                message = text,
                encoding = StandardCharsets.UTF_8,
                altText = text,
            )
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

    fun scanFrom(bitmap: Bitmap): ScanResult? {
        val result = barcodeReader.read(bitmap).firstOrNull() ?: return null
        val text = result.text?.takeIf { it.isNotBlank() } ?: return null
        return ScanResult(
            text = text,
            format = result.format.name,
        )
    }
}
