package nz.eloque.foss_wallet.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

@Composable
fun DisableTextScaling(
    fontScale: Float = 1f,
    content: @Composable () -> Unit,
) {
    val newDensity = Density(
        LocalDensity.current.density,
        fontScale = fontScale,
    )

    CompositionLocalProvider(
        LocalDensity provides newDensity,
    ) {
        content()
    }
}
