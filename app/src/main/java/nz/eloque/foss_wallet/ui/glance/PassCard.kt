package nz.eloque.foss_wallet.ui.glance

import android.content.Context
import android.graphics.Bitmap
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
import nz.eloque.foss_wallet.model.field.PassField

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

    // Fraction of the card's height that event_ticket_shape.xml's top-notch dips down
    // by (15.62dp out of the drawable's 90dp viewport height). The vector is stretched
    // to fillMaxSize() while keeping its 2:1 aspect ratio, so this fraction is constant
    // across all widget sizes.
    const val EVENT_NOTCH_DEPTH_FRACTION = 15.62f / 90f

    // Below this actual widget height, a barcode wouldn't be legible even if the tier
    // (which is derived from width only, see WidgetSizeTier.toTier) says LARGE — e.g. a
    // wide-but-short widget. Close to the LARGE bucket's nominal 180dp: the barcode only
    // gets whatever's left over after header, primary content and field rows, so even
    // a "LARGE" widget can leave it too little room unless we're this strict.
    val MIN_BARCODE_HEIGHT = 172.dp

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
    /** Number of extra secondary/auxiliary field rows shown below the primary content. */
    val extraFieldRows: Int,
) {
    SMALL(1f, 1),
    MEDIUM(1.4f, 2),
    LARGE(1.8f, 3),
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
    /**
     * Barcode bitmap, analogous to [Barcode]. Only shown from [WidgetSizeTier.LARGE]
     * onwards, and only if the widget's actual height clears [PassCardDefault.MIN_BARCODE_HEIGHT]
     * — a small barcode is worse than none, since it isn't scannable anyway.
     * If null (not loaded yet, or the pass has no barcode), nothing is rendered.
     */
    barcodeBitmap: Bitmap? = null,
) {
    val pass = localizedPass.pass
    val colors = pass.colors.toColorProviders()
    val size = LocalSize.current
    val tier = size.toTier()

    Box(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .clickable(onClick),
    ) {
        val shapeDrawable: Int? =
            when (pass.type) {
                is PassType.Coupon -> R.drawable.coupon_shape
                is PassType.Boarding -> R.drawable.boarding_pass_shape
                is PassType.Event -> R.drawable.event_ticket_shape
                is PassType.StoreCard -> R.drawable.store_card_shape
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
            if (pass.type is PassType.Event) {
                // event_ticket_shape.xml cuts a semicircular notch into the top edge,
                // centered horizontally. The regular card padding above isn't enough to
                // clear it, so add whatever's still missing before the header renders.
                val notchDepth = size.height * PassCardDefault.EVENT_NOTCH_DEPTH_FRACTION
                // The bare geometric depth cuts it close in practice (font metrics,
                // rounding), so pad the gap by roughly another quarter on top.
                val extraTopSpace = (notchDepth - PassCardDefault.padding.scaled(tier)) * 1.25f
                if (extraTopSpace > 0.dp) {
                    Box(modifier = GlanceModifier.height(extraTopSpace)) {}
                }
            }

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

            // Show as many secondary/auxiliary field rows as the current tier allows,
            // so bigger widgets actually use their extra space.
            FieldRows(
                secondaryFields = pass.secondaryFields,
                auxiliaryFields = pass.auxiliaryFields,
                colors = colors,
                tier = tier,
            )

            // On a large enough widget, additionally show the QR/barcode, analogous to
            // the standalone barcode view. Gated on actual height, not just the
            // width-derived tier, so a barcode never renders too small to scan.
            if (tier == WidgetSizeTier.LARGE && barcodeBitmap != null && size.height >= PassCardDefault.MIN_BARCODE_HEIGHT) {
                Box(modifier = GlanceModifier.height(6.dp.scaled(tier))) {}
                Image(
                    provider = ImageProvider(barcodeBitmap),
                    contentDescription = context.getString(R.string.barcode),
                    modifier =
                        GlanceModifier
                            .fillMaxWidth()
                            .defaultWeight(),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}

/**
 * Renders up to [WidgetSizeTier.extraFieldRows] rows of one secondary (left) and one
 * auxiliary (right) field each, mirroring the HeaderRow's two-column layout. Stops as
 * soon as a row has neither a secondary nor an auxiliary field left, so no empty rows
 * are rendered.
 */
@Composable
private fun FieldRows(
    secondaryFields: List<PassField>,
    auxiliaryFields: List<PassField>,
    colors: PassColorProviders,
    tier: WidgetSizeTier,
) {
    for (rowIndex in 0 until tier.extraFieldRows) {
        val secondaryField = secondaryFields.getOrNull(rowIndex)
        val auxiliaryField = auxiliaryFields.getOrNull(rowIndex)
        if (secondaryField == null && auxiliaryField == null) break

        Box(modifier = GlanceModifier.height(6.dp.scaled(tier))) {}
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            secondaryField?.let { field ->
                FieldColumn(
                    field = field,
                    colors = colors,
                    tier = tier,
                    modifier = GlanceModifier.defaultWeight(),
                    alignment = Alignment.Horizontal.Start,
                )
            }

            if (secondaryField != null && auxiliaryField != null) {
                Box(modifier = GlanceModifier.width(8.dp.scaled(tier))) {}
            }

            if (auxiliaryField != null) {
                FieldColumn(
                    field = auxiliaryField,
                    colors = colors,
                    tier = tier,
                    modifier = GlanceModifier.defaultWeight(),
                    alignment = Alignment.Horizontal.End,
                )
            } else {
                // Keeps the start column at half width when there's no auxiliary field.
                Box(modifier = GlanceModifier.defaultWeight()) {}
            }
        }
    }
}

@Composable
private fun FieldColumn(
    field: PassField,
    colors: PassColorProviders,
    tier: WidgetSizeTier,
    modifier: GlanceModifier,
    alignment: Alignment.Horizontal,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = alignment,
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
            // Left-aligned within its own column (instead of End) — the column itself
            // still sits to the right of the logo/title due to the Row's ordering.
            Column(horizontalAlignment = Alignment.Horizontal.Start) {
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
