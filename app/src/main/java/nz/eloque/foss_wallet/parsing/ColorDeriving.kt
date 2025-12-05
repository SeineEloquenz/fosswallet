package nz.eloque.foss_wallet.parsing

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.get
import nz.eloque.foss_wallet.model.PassColors

fun Bitmap?.derivePassColors(): PassColors? {
    if (this == null) return null
    
    val backgroundColor = this.backgroundColor() ?: return null
    val luminance = ColorUtils.calculateLuminance(backgroundColor.toArgb())
    val contentColor = if (luminance > 0.5) Color.Black else Color.White
    
    return PassColors(backgroundColor, contentColor, contentColor)
}

private fun Bitmap.backgroundColor(): Color? {
    val bitmapSize = 2 * this.width + 2 * this.height
    val threshold = 0.75
    val colorMap = HashMap<Color, Int>()
    for (i in 0 until this.width) {
        colorMap.merge(Color(this[i, 0]).clamp(), 1, Int::plus)
        colorMap.merge(Color(this[i, this.height - 1]).clamp(), 1, Int::plus)
    }
    for (i in 0 until this.height) {
        colorMap.merge(Color(this[0, i]).clamp(), 1, Int::plus)
        colorMap.merge(Color(this[this.width - 1, i]).clamp(), 1, Int::plus)
    }
    val foundColor = colorMap.entries.firstOrNull { (_, count) -> count > threshold * bitmapSize }?.key
    return foundColor?.coerceOpacity()
}

private fun Color.clamp(): Color {
    return this.copy(
        alpha = this.alpha.round(),
        red = this.red.round(),
        blue = this.blue.round(),
        green = this.green.round()
    )
}

private fun Float.round(): Float {
    return (this * 10).toInt() / 10f
}

private fun Color.coerceOpacity(): Color {
    return if (this.alpha == 0f) Color.White else this
}
