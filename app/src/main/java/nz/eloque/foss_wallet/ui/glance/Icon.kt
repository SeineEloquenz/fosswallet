package nz.eloque.foss_wallet.ui.glance

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.layout.size
import kotlin.math.roundToInt

@Composable
internal fun rememberVectorAsBitmap(
    image: ImageVector,
    density: Density,
    sizeDp: Dp,
    tint: Color = Color.Unspecified,
    layoutDirection: LayoutDirection = LayoutDirection.Ltr,
): Bitmap {
    val painter = rememberVectorPainter(image = image)
    val colorFilter = if (tint != Color.Unspecified) ColorFilter.tint(tint) else null
    return remember(image, tint, sizeDp, layoutDirection) {
        val sizePx = with(density) { sizeDp.toPx() }.roundToInt()
        val bitmap = ImageBitmap(sizePx, sizePx)
        val canvas = Canvas(bitmap)
        val size = Size(sizePx.toFloat(), sizePx.toFloat())
        CanvasDrawScope().draw(
            density = density,
            layoutDirection = layoutDirection,
            canvas = canvas,
            size = size,
        ) {
            with(painter) { draw(size = drawSize, colorFilter = colorFilter) }
        }
        bitmap.asAndroidBitmap()
    }
}

@Composable
fun Icon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: GlanceModifier = GlanceModifier,
    tint: Color = Color.Unspecified,
    size: Dp = 24.dp,
) {
    val context = LocalContext.current
    val density = Density(context.resources.displayMetrics.density)

    val bitmap =
        rememberVectorAsBitmap(
            image = imageVector,
            density = density,
            sizeDp = size,
            tint = tint,
            layoutDirection = context.layoutDirection(),
        )

    Image(
        provider = ImageProvider(bitmap),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
    )
}

private fun Context.layoutDirection(): LayoutDirection =
    if (resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
        LayoutDirection.Rtl
    } else {
        LayoutDirection.Ltr
    }
