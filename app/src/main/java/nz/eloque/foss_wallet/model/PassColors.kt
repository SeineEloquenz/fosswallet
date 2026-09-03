package nz.eloque.foss_wallet.model

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

data class PassColors(
    val background: Color,
    val foreground: Color,
    val label: Color,
) {
    companion object {
        val Fallback: PassColors
            @Composable @ReadOnlyComposable
            get() =
                PassColors(
                    background = MaterialTheme.colorScheme.surfaceVariant,
                    foreground = MaterialTheme.colorScheme.onSurface,
                    label = MaterialTheme.colorScheme.onSurface,
                )
    }
}
