package nz.eloque.foss_wallet.ui.screens.create

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.graphics.createBitmap

fun ContentResolver.scanSource(
    uri: Uri,
    mimeType: String? = null,
): ScanSource? {
    val mime = mimeType ?: getType(uri)
    if (mime?.startsWith("image/") == true) return ScanSource.Image
    if (mime == "application/pdf") return ScanSource.Pdf
    val name =
        query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
            if (it.moveToFirst()) it.getString(0) else null
        } ?: uri.lastPathSegment.orEmpty()
    return when (name.substringAfterLast('.').lowercase()) {
        "pdf" -> ScanSource.Pdf
        "jpg", "jpeg", "png", "webp", "gif", "bmp", "heic", "heif", "avif" -> ScanSource.Image
        else -> null
    }
}

data class PreviewPage(
    val bitmap: Bitmap,
    val pageCount: Int,
)

/** Open only the requested PDF page, keeping memory usage independent of page count. */
fun loadPreviewPage(
    resolver: ContentResolver,
    uri: Uri,
    pageIndex: Int,
    mimeType: String? = null,
): PreviewPage {
    if (resolver.scanSource(uri, mimeType) != ScanSource.Pdf) {
        val bitmap =
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val longest = maxOf(info.size.width, info.size.height)
                if (longest > 4096) {
                    decoder.setTargetSize(info.size.width * 4096 / longest, info.size.height * 4096 / longest)
                }
            }
        return PreviewPage(bitmap, 1)
    }
    return resolver.openFileDescriptor(uri, "r")!!.use { descriptor ->
        PdfRenderer(descriptor).use { renderer ->
            renderer.openPage(pageIndex).use { page ->
                val scale = minOf(3f, 4096f / maxOf(page.width, page.height))
                val bitmap = createBitmap((page.width * scale).toInt(), (page.height * scale).toInt())
                bitmap.eraseColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                PreviewPage(bitmap, renderer.pageCount)
            }
        }
    }
}
