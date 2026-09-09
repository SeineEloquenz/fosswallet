package nz.eloque.foss_wallet.ui.glance

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import nz.eloque.foss_wallet.MainActivity
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.launcher.PASS_ID_PARAM
import nz.eloque.foss_wallet.launcher.SHOW_BARCODE_PARAM
import nz.eloque.foss_wallet.model.LocalizedPassWithTags

@Composable
fun Barcode(
    localizedPass: LocalizedPassWithTags,
    barcodeBitmap: Bitmap?,
) {
    val context = LocalContext.current
    val tier = LocalSize.current.toTier()
    val pass = localizedPass.pass
    val colors = pass.colors.toColorProviders()
    Box(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .clickable(
                    actionStartActivity<MainActivity>(
                        parameters =
                            actionParametersOf(
                                PASS_ID_PARAM to pass.id,
                                SHOW_BARCODE_PARAM to true,
                            ),
                    ),
                ),
    ) {
        Column(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .padding(PassCardDefault.padding.scaled(tier)),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        ) {
            if (barcodeBitmap != null) {
                // No background/card behind the barcode at all — only the text below
                // gets the pass background, so the barcode's own black-on-white
                // contrast for scanning is never affected.
                Image(
                    provider = ImageProvider(barcodeBitmap),
                    contentDescription = context.getString(R.string.barcode),
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                    contentScale = ContentScale.Fit,
                )
                Box(modifier = GlanceModifier.height(4.dp.scaled(tier))) {}
            }
            Box(
                modifier =
                    GlanceModifier
                        .background(colors.background)
                        .cornerRadius(PassCardDefault.cornerRadius.scaled(tier))
                        .padding(horizontal = 6.dp.scaled(tier), vertical = 2.dp.scaled(tier)),
            ) {
                Text(
                    text = pass.description,
                    style =
                        TextStyle(
                            color = colors.foreground,
                            fontWeight = FontWeight.Medium,
                        ),
                    maxLines = 2,
                )
            }
        }
    }
}
