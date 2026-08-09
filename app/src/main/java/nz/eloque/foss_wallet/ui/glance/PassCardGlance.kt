package nz.eloque.foss_wallet.ui.card.glance

import android.content.Context
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
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.defaultWeight
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
import java.io.File

private object PassCardGlanceDefaults {
    val padding = 8.dp
    val cornerRadius = 12.dp
    val logoSize = 28.dp

    fun fallbackColors(): PassColors =
        PassColors(
            background = Color(0xFFE7E0EC),
            foreground = Color(0xFF1D1B20),
            label = Color(0xFF49454F),
        )
}

@Composable
fun PassCardGlance(
    localizedPass: LocalizedPassWithTags,
    context: Context,
    onClick: (() -> Unit)? = null,
) {
    val pass = localizedPass.pass
    val colors = pass.colors ?: PassCardGlanceDefaults.fallbackColors()
    val isEventTicket = pass.type is PassType.Event
    val isCoupon = pass.type is PassType.Coupon

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .let { base -> if (onClick != null) base.clickable(onClick) else base },
    ) {
        when {
            isEventTicket -> {
                Image(
                    provider = ImageProvider(R.drawable.event_ticket_shape),
                    contentDescription = null,
                    modifier = GlanceModifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                    colorFilter = ColorFilter.tint(ColorProvider(colors.background)),
                )
            }
            isCoupon -> {
                Image(
                    provider = ImageProvider(R.drawable.coupon_shape),
                    contentDescription = null,
                    modifier = GlanceModifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                    colorFilter = ColorFilter.tint(ColorProvider(colors.background)),
                )
            }
            else -> {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(colors.background))
                        .cornerRadius(PassCardGlanceDefaults.cornerRadius),
                ) {}
            }
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(
                    start = PassCardGlanceDefaults.padding,
                    end = PassCardGlanceDefaults.padding,
                    bottom = PassCardGlanceDefaults.padding,
                    top = PassCardGlanceDefaults.padding + if (isEventTicket) 12.dp else 0.dp,
                ),
        ) {
            HeaderRowGlance(
                localizedPass = localizedPass,
                context = context,
                foreground = colors.foreground,
                labelColor = colors.label,
            )
            Box(modifier = GlanceModifier.height(4.dp)) {}
            PrimaryContentGlance(
                localizedPass = localizedPass,
                context = context,
                foreground = colors.foreground,
                labelColor = colors.label,
            )
        }
    }
}

@Composable
private fun HeaderRowGlance(
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
                modifier = GlanceModifier
                    .width(PassCardGlanceDefaults.logoSize)
                    .height(PassCardGlanceDefaults.logoSize),
                contentScale = ContentScale.Fit,
            )
            Box(modifier = GlanceModifier.width(6.dp)) {}
        }

        if (pass.logoText != null) {
            Text(
                text = pass.logoText,
                maxLines = 1,
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(
                    color = ColorProvider(foreground),
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
                        style = TextStyle(
                            color = ColorProvider(labelColor),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                        ),
                    )
                }
                Text(
                    text = firstHeaderField.content.prettyPrint(),
                    maxLines = 1,
                    style = TextStyle(
                        color = ColorProvider(foreground),
                        fontSize = 11.sp,
                    ),
                )
            }
        }
    }
}

@Composable
private fun PrimaryContentGlance(
    localizedPass: LocalizedPassWithTags,
    context: Context,
    foreground: Color,
    labelColor: Color,
) {
    val pass = localizedPass.pass
    val primaryField = pass.primaryFields.getOrElse(0) { PassField.Empty }

    val previewFile: File? = when (pass.type) {
        is PassType.Boarding -> null
        is PassType.Coupon, PassType.StoreCard -> pass.stripFile(context)
        is PassType.Event -> if (pass.hasStrip) pass.stripFile(context) else pass.thumbnailFile(context)
        is PassType.Generic -> pass.thumbnailFile(context)
    }
    val previewBitmap = remember(previewFile) { previewFile?.let { loadBitmap(it) } }

    Row(
        modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            primaryField.label?.let { label ->
                Text(
                    text = label,
                    maxLines = 1,
                    style = TextStyle(
                        color = ColorProvider(labelColor),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                    ),
                )
            }
            Text(
                text = primaryField.content.prettyPrint(),
                maxLines = 1,
                style = TextStyle(
                    color = ColorProvider(foreground),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                ),
            )
        }
        if (previewBitmap != null) {
            Image(
                provider = ImageProvider(previewBitmap),
                contentDescription = null,
                modifier = GlanceModifier
                    .width(48.dp)
                    .height(48.dp)
                    .cornerRadius(6.dp),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

private fun loadBitmap(file: File?): android.graphics.Bitmap? {
    if (file == null || !file.exists()) return null
    return try {
        BitmapFactory.decodeFile(file.absolutePath)
    } catch (e: Exception) {
        null
    }
}
