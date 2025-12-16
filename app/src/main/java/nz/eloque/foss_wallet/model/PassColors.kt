package nz.eloque.foss_wallet.model

import androidx.compose.material3.CardColors
import androidx.compose.ui.graphics.Color

data class PassColors(
    val background: Color,
    val foreground: Color,
    val label: Color,
) {
    fun toCardColors(): CardColors {
        return CardColors(background, foreground, background.copy(alpha = 0.38f), foreground.copy(alpha = 0.38f))
    }
}
