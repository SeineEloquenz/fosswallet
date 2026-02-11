package nz.eloque.foss_wallet.model

import androidx.compose.material3.CardColors
import androidx.compose.ui.graphics.Color

data class PassColors(
    val background: Color,
    val foreground: Color,
    val label: Color,
) {
    fun toCardColors(): CardColors {
        val opaqueBackground = background.copy(alpha = 1f)
        val opaqueForeground = foreground.copy(alpha = 1f)
        return CardColors(
            opaqueBackground,
            opaqueForeground,
            opaqueBackground.copy(alpha = 0.38f),
            opaqueForeground.copy(alpha = 0.38f)
        )
    }
}
