package nz.eloque.foss_wallet.model

import androidx.compose.material3.CardColors
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class PassColors(
    @Contextual val background: Color,
    @Contextual val foreground: Color,
    @Contextual val label: Color,
) {
    fun toCardColors(): CardColors = CardColors(background, foreground, background.copy(alpha = 0.38f), foreground.copy(alpha = 0.38f))
}
