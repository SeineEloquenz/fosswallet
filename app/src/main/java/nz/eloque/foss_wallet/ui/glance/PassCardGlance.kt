package nz.eloque.foss_wallet.ui.glance

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
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
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.LocalizedPassWithTags
import nz.eloque.foss_wallet.model.PassColors
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.model.field.PassField
import java.io.File

private object PassCardDefault {
    val padding = 8.dp
    val cornerRadius = 12.dp
    val logoSize = 28.dp
    val flipIconSize = 20.dp
    const val bitmapTargetSizePx = 128

    fun fallbackColors(): PassColors =
        PassColors(
            background = Color(0xFFE7E0EC),
            foreground = Color(0xFF1D1B20),
            label = Color(0xFF49454F),
        )
}

/**
 * Wraps a fixed [Color] in a [ColorProvider].
 *
 * Glance's `ColorProvider(color: Color)` shorthand is restricted to the
 * `androidx.glance` library group. The public, documented way to get a
 * ColorProvider for a single fixed color is to supply the same value for
 * both `day` and `night`.
 */
private fun fixedColorProvider(color: Color): ColorProvider = ColorProvider(day = color, night = color)

@Composable
fun PassCardFront(
    localizedPass: LocalizedPassWithTags,
    context: Context,
    onClick: Action,
) {
    val pass = localizedPass.pass
    val colors = pass.colors ?: PassCardDefault.fallbackColors()
    val isEventTicket = pass.type is PassType.Event
    val isCoupon = pass.type is PassType.Coupon

    Box(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .clickable(onClick),
    ) {
        when {
            isEventTicket -> {
                Image(
                    provider = ImageProvider(R.drawable.event_ticket_shape),
                    contentDescription = null,
                    modifier = GlanceModifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                    colorFilter = ColorFilter.tint(fixedColorProvider(colors.background)),
                )
            }

            isCoupon -> {
                Image(
                    provider = ImageProvider(R.drawable.coupon_shape),
                    contentDescription = null,
                    modifier = GlanceModifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                    colorFilter = ColorFilter.tint(fixedColorProvider(colors.background)),
                )
            }

            else -> {
                Box(
                    modifier =
                        GlanceModifier
                            .fillMaxSize()
                            .background(fixedColorProvider(colors.background))
                            .cornerRadius(PassCardDefault.cornerRadius),
                ) {}
            }
        }

        Column(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .padding(
                        start = PassCardDefault.padding,
                        end = PassCardDefault.padding,
                        bottom = PassCardDefault.padding,
                        top = PassCardDefault.padding + if (isEventTicket) 12.dp else 0.dp,
                    ),
        ) {
            HeaderRow(
                localizedPass = localizedPass,
                context = context,
                foreground = colors.foreground,
                labelColor = colors.label,
            )
            Box(modifier = GlanceModifier.height(4.dp)) {}
            PrimaryContent(
                localizedPass = localizedPass,
                context = context,
                foreground = colors.foreground,
                labelColor = colors.label,
            )
        }

        // Flip-to-back icon overlay, top-end corner. No functionality yet.
        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .padding(
                        top = PassCardDefault.padding + if (isEventTicket) 12.dp else 0.dp,
                        end = PassCardDefault.padding,
                    ),
            contentAlignment = Alignment.TopEnd,
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_flip_to_back),
                contentDescription = null,
                modifier =
                    GlanceModifier
                        .width(PassCardDefault.flipIconSize)
                        .height(PassCardDefault.flipIconSize),
                colorFilter = ColorFilter.tint(fixedColorProvider(colors.foreground)),
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
                        .width(PassCardDefault.logoSize)
                        .height(PassCardDefault.logoSize),
                contentScale = ContentScale.Fit,
            )
            Box(modifier = GlanceModifier.width(6.dp)) {}
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
                        fontSize = 13.sp,
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
                                fontSize = 9.sp,
                            ),
                    )
                }
                Text(
                    text = firstHeaderField.content.prettyPrint(),
                    maxLines = 1,
                    style =
                        TextStyle(
                            color = fixedColorProvider(foreground),
                            fontSize = 11.sp,
                        ),
                )
            }
        }
    }
}

@Composable
private fun PrimaryContent(
    localizedPass: LocalizedPassWithTags,
    context: Context,
    foreground: Color,
    labelColor: Color,
) {
    val pass = localizedPass.pass
    val primaryField = pass.primaryFields.getOrElse(0) { PassField.Empty }

    val previewFile: File? =
        when (pass.type) {
            is PassType.Boarding -> null
            is PassType.Coupon, PassType.StoreCard -> pass.stripFile(context)
            is PassType.Event -> if (pass.hasStrip) pass.stripFile(context) else pass.thumbnailFile(context)
            is PassType.Generic -> pass.thumbnailFile(context)
        }
    val previewBitmap = remember(previewFile) { previewFile?.let { loadBitmap(it) } }

    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            primaryField.label?.let { label ->
                Text(
                    text = label,
                    maxLines = 1,
                    style =
                        TextStyle(
                            color = fixedColorProvider(labelColor),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                        ),
                )
            }
            Text(
                text = primaryField.content.prettyPrint(),
                maxLines = 1,
                style =
                    TextStyle(
                        color = fixedColorProvider(foreground),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    ),
            )
        }
        if (previewBitmap != null) {
            Image(
                provider = ImageProvider(previewBitmap),
                contentDescription = null,
                modifier =
                    GlanceModifier
                        .width(48.dp)
                        .height(48.dp)
                        .cornerRadius(6.dp),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

private fun loadBitmap(
    file: File?,
    targetSizePx: Int = PassCardDefault.bitmapTargetSizePx,
): Bitmap? {
    if (file == null || !file.exists()) return null
    return try {
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, boundsOptions)

        val decodeOptions =
            BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(boundsOptions, targetSizePx, targetSizePx)
            }
        BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
    } catch (_: Exception) {
        null
    }
}

private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int,
): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}
