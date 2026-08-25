package nz.eloque.foss_wallet.ui.glance

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlipToBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.LocalizedPassWithTags
import nz.eloque.foss_wallet.model.PassColors
import nz.eloque.foss_wallet.model.PassType

internal object PassCardDefault {
    // Base values, tuned for the SMALL tier (180x90dp). Scaled up via WidgetSizeTier.scale
    // for larger widget sizes so bigger widgets actually use their extra space.
    val padding = 8.dp
    val cornerRadius = 12.dp
    val logoSize = 28.dp
    val iconSize = 24.dp
    const val TITLE_FONT_SP = 13
    const val LABEL_FONT_SP = 9
    const val CONTENT_FONT_SP = 11
    const val BITMAP_TARGET_SIZE_PX = 128

    fun fallbackColors(): PassColors =
        PassColors(
            background = Color(0xFFE7E0EC),
            foreground = Color(0xFF1D1B20),
            label = Color(0xFF49454F),
        )
}

/** Wraps a fixed [Color] in a [ColorProvider]. */
internal fun fixedColorProvider(color: Color): ColorProvider = ColorProvider(day = color, night = color)

/**
 * Matches the three [androidx.glance.appwidget.SizeMode.Responsive] buckets declared in
 * Widget.kt (180x90 / 270x135 / 360x180dp). [LocalSize] snaps to the closest of
 * these, so this maps 1:1 to whichever bucket the launcher picked.
 */
internal enum class WidgetSizeTier(val scale: Float) {
    SMALL(1f),
    MEDIUM(1.4f),
    LARGE(1.8f),
}

internal fun DpSize.toTier(): WidgetSizeTier =
    when {
        width < 270.dp -> WidgetSizeTier.SMALL
        width < 360.dp -> WidgetSizeTier.MEDIUM
        else -> WidgetSizeTier.LARGE
    }

internal fun Dp.scaled(tier: WidgetSizeTier): Dp = this * tier.scale

internal fun Int.scaledSp(tier: WidgetSizeTier) = (this * tier.scale).sp

@Composable
fun PassCardFront(
    localizedPass: LocalizedPassWithTags,
    context: Context,
    onClick: Action,
    onFlipToBack: Action,
) {
    val pass = localizedPass.pass
    val colors = pass.colors ?: PassCardDefault.fallbackColors()
    val tier = LocalSize.current.toTier()

    Box(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .clickable(onClick),
    ) {
        val shapeDrawable: Int? =
            when (pass.type) {
                is PassType.Event -> R.drawable.event_ticket_shape
                is PassType.Coupon -> R.drawable.coupon_shape
                is PassType.Boarding -> R.drawable.boarding_pass_shape
                else -> null
            }

        if (shapeDrawable != null) {
            Image(
                provider = ImageProvider(shapeDrawable),
                contentDescription = null,
                modifier = GlanceModifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
                colorFilter = ColorFilter.tint(fixedColorProvider(colors.background)),
            )
        } else {
            Box(
                modifier =
                    GlanceModifier
                        .fillMaxSize()
                        .background(fixedColorProvider(colors.background))
                        .cornerRadius(PassCardDefault.cornerRadius.scaled(tier)),
            ) {}
        }

        Column(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .padding(PassCardDefault.padding.scaled(tier)),
        ) {
            HeaderRow(
                localizedPass = localizedPass,
                context = context,
                foreground = colors.foreground,
                labelColor = colors.label,
                tier = tier,
            )
            Box(modifier = GlanceModifier.height(4.dp.scaled(tier))) {}
            if (pass.type is PassType.Boarding) {
                BoardingPrimary(
                    localizedPass = localizedPass,
                    context = context,
                    foreground = colors.foreground,
                )
            } else {
                PrimaryContent(
                    localizedPass = localizedPass,
                    context = context,
                    foreground = colors.foreground,
                    labelColor = colors.label,
                )
            }
        }

        // Flip-to-back icon overlay, top-end corner
        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .padding(PassCardDefault.padding.scaled(tier)),
            contentAlignment = Alignment.TopEnd,
        ) {
            Icon(
                imageVector = Icons.Filled.FlipToBack,
                contentDescription = context.getString(R.string.flip_to_back),
                modifier = GlanceModifier.clickable(onFlipToBack),
                tint = colors.foreground,
                size = PassCardDefault.iconSize.scaled(tier),
            )
        }
    }
}

@Composable
private fun HeaderRow(
    localizedPass: LocalizedPassWithTags,
    context: Context,
    foreground: Color,
    labelColor: Color,
    tier: WidgetSizeTier,
) {
    val pass = localizedPass.pass
    val logoBitmap = remember(pass.logoFile(context)) { loadBitmap(pass.logoFile(context)) }
    val firstHeaderField = pass.headerFields.firstOrNull()

    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        if (logoBitmap != null) {
            Image(
                provider = ImageProvider(logoBitmap),
                contentDescription = null,
                modifier =
                    GlanceModifier
                        .width(PassCardDefault.logoSize.scaled(tier))
                        .height(PassCardDefault.logoSize.scaled(tier)),
                contentScale = ContentScale.Fit,
            )
            Box(modifier = GlanceModifier.width(6.dp.scaled(tier))) {}
        }

        if (pass.logoText != null) {
            Text(
                text = pass.logoText,
                maxLines = 1,
                modifier = GlanceModifier.defaultWeight(),
                style =
                    TextStyle(
                        color = fixedColorProvider(foreground),
                        fontWeight = FontWeight.Bold,
                        fontSize = PassCardDefault.TITLE_FONT_SP.scaledSp(tier),
                    ),
            )
        } else {
            Box(modifier = GlanceModifier.defaultWeight()) {}
        }

        if (firstHeaderField != null) {
            Column(horizontalAlignment = Alignment.Horizontal.End) {
                firstHeaderField.label?.let { label ->
                    Text(
                        text = label,
                        maxLines = 1,
                        style =
                            TextStyle(
                                color = fixedColorProvider(labelColor),
                                fontWeight = FontWeight.Bold,
                                fontSize = PassCardDefault.LABEL_FONT_SP.scaledSp(tier),
                            ),
                    )
                }
                Text(
                    text = firstHeaderField.content.prettyPrint(),
                    maxLines = 1,
                    style =
                        TextStyle(
                            color = fixedColorProvider(foreground),
                            fontSize = PassCardDefault.CONTENT_FONT_SP.scaledSp(tier),
                        ),
                )
            }
        }
    }
}
