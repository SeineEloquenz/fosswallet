package nz.eloque.foss_wallet.ui.glance

import android.content.Context
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
    const val TITLE_FONT_SP = 13
    const val LABEL_FONT_SP = 9
    const val CONTENT_FONT_SP = 11
    const val BITMAP_TARGET_SIZE_PX = 128

    // Used only when the pass itself doesn't specify colors (PassColors == null).
    // Unlike a pass's own brand colors, this should follow the system's day/night
    // mode — otherwise an unbranded pass renders as a light card even in dark mode.
    val fallbackColorProviders =
        PassColorProviders(
            background = ColorProvider(day = Color(0xFFE7E0EC), night = Color(0xFF2B2930)),
            foreground = ColorProvider(day = Color(0xFF1D1B20), night = Color(0xFFE6E0E9)),
            label = ColorProvider(day = Color(0xFF49454F), night = Color(0xFFCAC4D0)),
        )
}

/** Wraps a fixed [Color] in a [ColorProvider] — same value for day and night. */
internal fun fixedColorProvider(color: Color): ColorProvider = ColorProvider(day = color, night = color)

/** [ColorProvider] equivalents of [PassColors]' three fields. */
internal data class PassColorProviders(
    val background: ColorProvider,
    val foreground: ColorProvider,
    val label: ColorProvider,
)

/**
 * Resolves a pass's colors to [PassColorProviders]. A pass's own colors are brand
 * colors set by the issuer, so they stay fixed regardless of day/night mode. Only the
 * fallback (no colors on the pass) follows the system theme — see
 * [PassCardDefault.fallbackColorProviders].
 */
internal fun PassColors?.toColorProviders(): PassColorProviders =
    if (this != null) {
        PassColorProviders(
            background = fixedColorProvider(background),
            foreground = fixedColorProvider(foreground),
            label = fixedColorProvider(label),
        )
    } else {
        PassCardDefault.fallbackColorProviders
    }

/**
 * Matches the three [androidx.glance.appwidget.SizeMode.Responsive] buckets declared in
 * Widget.kt (180x90 / 270x135 / 360x180dp). [LocalSize] snaps to the closest of
 * these, so this maps 1:1 to whichever bucket the launcher picked.
 */
internal enum class WidgetSizeTier(
    val scale: Float,
) {
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
fun PassCard(
    localizedPass: LocalizedPassWithTags,
    context: Context,
    onClick: Action,
) {
    val pass = localizedPass.pass
    val colors = pass.colors.toColorProviders()
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
                colorFilter = ColorFilter.tint(colors.background),
            )
        } else {
            Box(
                modifier =
                    GlanceModifier
                        .fillMaxSize()
                        .background(colors.background)
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

            // Show up to two more fields (first secondary + first auxiliary) side
            // by side so the widget isn't so empty at larger sizes.
            val secondaryField = pass.secondaryFields.firstOrNull()
            val auxiliaryField = pass.auxiliaryFields.firstOrNull()

            if (secondaryField != null || auxiliaryField != null) {
                Box(modifier = GlanceModifier.height(6.dp.scaled(tier))) {}
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    secondaryField?.let { field ->
                        Column(modifier = GlanceModifier.defaultWeight()) {
                            field.label?.let { label ->
                                Text(
                                    text = label,
                                    maxLines = 1,
                                    style =
                                        TextStyle(
                                            color = colors.label,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = PassCardDefault.LABEL_FONT_SP.scaledSp(tier),
                                        ),
                                )
                            }
                            Text(
                                text = field.content.prettyPrint(),
                                maxLines = if (tier == WidgetSizeTier.SMALL) 1 else 2,
                                style =
                                    TextStyle(
                                        color = colors.foreground,
                                        fontSize = PassCardDefault.CONTENT_FONT_SP.scaledSp(tier),
                                    ),
                            )
                        }
                    }

                    auxiliaryField?.let { field ->
                        if (secondaryField != null) {
                            Box(modifier = GlanceModifier.width(8.dp.scaled(tier))) {}
                        }
                        Column(
                            modifier = GlanceModifier.defaultWeight(),
                            horizontalAlignment = Alignment.Horizontal.End,
                        ) {
                            field.label?.let { label ->
                                Text(
                                    text = label,
                                    maxLines = 1,
                                    style =
                                        TextStyle(
                                            color = colors.label,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = PassCardDefault.LABEL_FONT_SP.scaledSp(tier),
                                        ),
                                )
                            }
                            Text(
                                text = field.content.prettyPrint(),
                                maxLines = if (tier == WidgetSizeTier.SMALL) 1 else 2,
                                style =
                                    TextStyle(
                                        color = colors.foreground,
                                        fontSize = PassCardDefault.CONTENT_FONT_SP.scaledSp(tier),
                                    ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderRow(
    localizedPass: LocalizedPassWithTags,
    context: Context,
    foreground: ColorProvider,
    labelColor: ColorProvider,
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
                maxLines = if (tier == WidgetSizeTier.SMALL) 1 else 2,
                modifier = GlanceModifier.defaultWeight(),
                style =
                    TextStyle(
                        color = foreground,
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
                                color = labelColor,
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
                            color = foreground,
                            fontSize = PassCardDefault.CONTENT_FONT_SP.scaledSp(tier),
                        ),
                )
            }
        }
    }
}
