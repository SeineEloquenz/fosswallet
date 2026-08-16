package nz.eloque.foss_wallet.ui.glance

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlipToFront
import androidx.compose.runtime.Composable
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
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.LocalizedPassWithTags
import nz.eloque.foss_wallet.model.PassType

@Composable
fun PassCardBack(
    localizedPass: LocalizedPassWithTags,
    context: Context,
    barcodeBitmap: Bitmap?,
    onClick: Action,
    onFlipToFront: Action,
) {
    val pass = localizedPass.pass
    val colors = pass.colors ?: PassCardDefault.fallbackColors()
    val isEventTicket = pass.type is PassType.Event
    val isCoupon = pass.type is PassType.Coupon
    val isBoardingPass = pass.type is PassType.Boarding

    Box(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .clickable(onClick),
    ) {
        val shapeDrawableRes =
            when {
                isEventTicket -> R.drawable.event_ticket_shape
                isCoupon -> R.drawable.coupon_shape
                isBoardingPass -> R.drawable.boarding_pass_shape
                else -> null
            }

        if (shapeDrawableRes != null) {
            Image(
                provider = ImageProvider(shapeDrawableRes),
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
                        .cornerRadius(PassCardDefault.cornerRadius),
            ) {}
        }

        Row(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .padding(
                        start = PassCardDefault.padding,
                        end = PassCardDefault.padding,
                        bottom = PassCardDefault.padding,
                        top = PassCardDefault.padding + if (isEventTicket) 12.dp else 0.dp,
                    ),
            verticalAlignment = Alignment.Vertical.CenterVertically,
        ) {
            // Left: QR code.
            if (barcodeBitmap != null) {
                Image(
                    provider = ImageProvider(barcodeBitmap),
                    contentDescription = null,
                    modifier =
                        GlanceModifier
                            .width(64.dp)
                            .height(64.dp),
                    contentScale = ContentScale.Fit,
                )
                Box(modifier = GlanceModifier.width(8.dp)) {}
            }

            // Right: title + first secondary field. No footer, no tags, no calendar/card button.
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = pass.description,
                    maxLines = 2,
                    style =
                        TextStyle(
                            color = fixedColorProvider(colors.foreground),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                        ),
                )
                pass.secondaryFields.firstOrNull()?.let { field ->
                    Box(modifier = GlanceModifier.height(4.dp)) {}
                    field.label?.let { label ->
                        Text(
                            text = label,
                            maxLines = 1,
                            style =
                                TextStyle(
                                    color = fixedColorProvider(colors.label),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                ),
                        )
                    }
                    Text(
                        text = field.content.prettyPrint(),
                        maxLines = 1,
                        style =
                            TextStyle(
                                color = fixedColorProvider(colors.foreground),
                                fontSize = 11.sp,
                            ),
                    )
                }
            }
        }

        // Flip-to-front icon overlay, top-end corner.
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
            Icon(
                imageVector = Icons.Filled.FlipToFront,
                contentDescription = context.getString(R.string.flip_to_front),
                modifier = GlanceModifier.clickable(onFlipToFront),
                tint = colors.foreground,
            )
        }
    }
}
