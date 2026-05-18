package nz.eloque.foss_wallet.ui.screens.create

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import zxingcpp.BarcodeReader
import kotlin.math.hypot
import kotlin.math.max

object ImageScanner {
    data class ScanResult(
        val text: String,
        val format: String,
    )

    fun scanFrom(
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
        scanBitmap(bitmap)?.let { return it }

        val locatedResult =
            BarcodeReaders
                .symbolLocator
                .read(bitmap)
                .firstOrNull()
                ?: return null
        val position = locatedResult.position

        for (correctedBitmap in perspectiveCorrectionCandidates(bitmap, position)) {
            scanBitmap(correctedBitmap)?.let { return it }
        }

        return null
    }

    private fun scanBitmap(bitmap: Bitmap): ScanResult? {
        BarcodeReaders.decoders.forEach { reader ->
            val results = reader.read(bitmap)
            val result = results.firstOrNull { it.error == null } ?: return@forEach
            val text = result.text?.takeIf { it.isNotBlank() } ?: return@forEach
            return ScanResult(
                text = text,
                format = result.format.name,
            )
        }

        return null
    }

    private fun perspectiveCorrectionCandidates(
        bitmap: Bitmap,
        position: BarcodeReader.Position,
    ): List<Bitmap> {
        val points =
            floatArrayOf(
                position.topLeft.x.toFloat(),
                position.topLeft.y.toFloat(),
                position.topRight.x.toFloat(),
                position.topRight.y.toFloat(),
                position.bottomRight.x.toFloat(),
                position.bottomRight.y.toFloat(),
                position.bottomLeft.x.toFloat(),
                position.bottomLeft.y.toFloat(),
            )

        return listOf(1.35f, 1.8f)
            .flatMap { scale ->
                val expandedPoints = expandAroundCenter(points, scale)
                val corrected = perspectiveCorrect(bitmap, expandedPoints)
                listOf(corrected, enhanceContrast(corrected))
            }
    }

    private fun expandAroundCenter(
        points: FloatArray,
        scale: Float,
    ): FloatArray {
        var centerX = 0f
        var centerY = 0f
        for (index in points.indices step 2) {
            centerX += points[index]
            centerY += points[index + 1]
        }
        centerX /= 4f
        centerY /= 4f

        return FloatArray(points.size) { index ->
            val center = if (index % 2 == 0) centerX else centerY
            center + (points[index] - center) * scale
        }
    }

    private fun perspectiveCorrect(
        bitmap: Bitmap,
        sourcePoints: FloatArray,
    ): Bitmap {
        val width =
            max(
                distance(sourcePoints, 0, 2),
                distance(sourcePoints, 6, 4),
            ).toInt()
        val height =
            max(
                distance(sourcePoints, 0, 6),
                distance(sourcePoints, 2, 4),
            ).toInt()
        val margin = max(24, height / 4)

        val destinationPoints =
            floatArrayOf(
                margin.toFloat(),
                margin.toFloat(),
                (width + margin).toFloat(),
                margin.toFloat(),
                (width + margin).toFloat(),
                (height + margin).toFloat(),
                margin.toFloat(),
                (height + margin).toFloat(),
            )

        val matrix =
            Matrix().apply {
                setPolyToPoly(sourcePoints, 0, destinationPoints, 0, 4)
            }
        val output =
            Bitmap.createBitmap(
                width + margin * 2,
                height + margin * 2,
                Bitmap.Config.ARGB_8888,
            )
        Canvas(output).apply {
            drawColor(Color.WHITE)
            drawBitmap(
                bitmap,
                matrix,
                Paint(Paint.FILTER_BITMAP_FLAG),
            )
        }
        return output
    }

    private fun enhanceContrast(bitmap: Bitmap): Bitmap {
        val contrast = 2.5f
        val translate = (-0.5f * contrast + 0.5f) * 255f
        val colorMatrix =
            ColorMatrix(
                floatArrayOf(
                    contrast,
                    0f,
                    0f,
                    0f,
                    translate,
                    0f,
                    contrast,
                    0f,
                    0f,
                    translate,
                    0f,
                    0f,
                    contrast,
                    0f,
                    translate,
                    0f,
                    0f,
                    0f,
                    1f,
                    0f,
                ),
            )
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        Canvas(output).drawBitmap(
            bitmap,
            0f,
            0f,
            Paint(Paint.FILTER_BITMAP_FLAG).apply {
                colorFilter = ColorMatrixColorFilter(colorMatrix)
            },
        )
        return output
    }

    private fun distance(
        points: FloatArray,
        startIndex: Int,
        endIndex: Int,
    ): Float =
        hypot(
            points[startIndex] - points[endIndex],
            points[startIndex + 1] - points[endIndex + 1],
        )
}
