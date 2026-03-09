package nz.eloque.foss_wallet.ui.screens.create

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import zxingcpp.BarcodeReader

object ImageScanner {
    private val barcodeReader = BarcodeReader(
        BarcodeReader.Options(
            tryHarder = true,
            tryRotate = true,
            tryInvert = true,
        )
    )

    data class ScanResult(
        val text: String,
        val format: String,
    )

    fun scanFrom(contentResolver: ContentResolver, uri: Uri): ScanResult? {
        val source = ImageDecoder.createSource(contentResolver, uri)
        val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, source ->
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
