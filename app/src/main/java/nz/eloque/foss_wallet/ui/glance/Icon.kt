package nz.eloque.foss_wallet.ui.glance

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.layout.size

@Composable
fun Icon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: GlanceModifier = GlanceModifier,
    tint: Color = Color.Unspecified,
    size: Dp = 24.dp
) {
    val context = LocalContext.current
    val density = Density(context.resources.displayMetrics.density)

    val bitmap = rememberVectorAsBitmap(
        image = imageVector,
        density = density,
        sizeDp = size,
        tint = tint
    )

    Image(
        provider = ImageProvider(bitmap),
        contentDescription = contentDescription,
        modifier = modifier.size(size)
    )
}
