package nz.eloque.foss_wallet.ui.glance

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Forward
import androidx.compose.material.icons.filled.Train
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.LocalizedPassWithTags
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.model.TransitType
import nz.eloque.foss_wallet.model.field.PassField
import java.io.File

@Composable
internal fun PrimaryContent(
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

@Composable
internal fun BoardingPrimary(
    localizedPass: LocalizedPassWithTags,
    foreground: Color,
) {
    val pass = localizedPass.pass
    val primaryFields = pass.primaryFields
    val departureField = primaryFields.getOrNull(0)
    val destinationField = primaryFields.getOrNull(1)
    val transitType = (pass.type as? PassType.Boarding)?.transitType

    val icon: ImageVector? =
        when (transitType) {
            TransitType.AIR -> Icons.Filled.Flight
            TransitType.TRAIN -> Icons.Filled.Train
            TransitType.BUS -> Icons.AutoMirrored.Filled.DirectionsBus
            TransitType.BOAT -> Icons.AutoMirrored.Filled.DirectionsBoat
            TransitType.GENERIC -> Icons.AutoMirrored.Default.Forward
            null -> null
        }

    Row(
        modifier = GlanceModifier.fillMaxWidth().height(32.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        Text(
            text = departureField?.content?.prettyPrint().orEmpty(),
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight(),
            style =
                TextStyle(
                    color = fixedColorProvider(foreground),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                ),
        )

        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = resources.getString(R.string.to),
                tint = foreground,
            )
        }

        Text(
            text = destinationField?.content?.prettyPrint().orEmpty(),
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight(),
            style =
                TextStyle(
                    color = fixedColorProvider(foreground),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.End,
                ),
        )
    }
}

internal fun loadBitmap(
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
