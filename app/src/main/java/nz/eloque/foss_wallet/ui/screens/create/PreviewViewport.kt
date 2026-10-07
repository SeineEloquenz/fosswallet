package nz.eloque.foss_wallet.ui.screens.create

import kotlin.math.max
import kotlin.math.min

/** Image coordinates are mapped into the viewport by x * scale + left (and likewise for y). */
data class PreviewViewport(
    val imageWidth: Int,
    val imageHeight: Int,
    val width: Int,
    val height: Int,
    val zoom: Float = 1f,
    val panX: Float = 0f,
    val panY: Float = 0f,
) {
    val scale = min(width.toFloat() / imageWidth, height.toFloat() / imageHeight) * zoom
    val left = (width - imageWidth * scale) / 2f + panX
    val top = (height - imageHeight * scale) / 2f + panY

    /** Scale around the previous finger centroid, then follow its movement without clamping to the file. */
    fun transformed(
        centroidX: Float,
        centroidY: Float,
        dragX: Float,
        dragY: Float,
        zoomFactor: Float,
    ): PreviewViewport {
        val nextZoom = zoom * zoomFactor
        val ratio = nextZoom / zoom
        val nextPanX = (panX + width / 2f - centroidX) * ratio + centroidX - width / 2f + dragX
        val nextPanY = (panY + height / 2f - centroidY) * ratio + centroidY - height / 2f + dragY
        return copy(
            zoom = nextZoom,
            panX = nextPanX,
            panY = nextPanY,
        )
    }

    fun visibleBounds(): ImageBounds? {
        val x0 = max(0f, -left / scale).toInt()
        val y0 = max(0f, -top / scale).toInt()
        val x1 = min(imageWidth.toFloat(), (width - left) / scale).toInt()
        val y1 = min(imageHeight.toFloat(), (height - top) / scale).toInt()
        return if (x1 > x0 && y1 > y0) ImageBounds(x0, y0, x1 - x0, y1 - y0) else null
    }
}

data class ImageBounds(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)
