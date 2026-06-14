package nz.eloque.foss_wallet.ui.screens.pass

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import nz.eloque.compose_kit.dialog.FullscreenDialog
import nz.eloque.compose_kit.effect.UpdateBrightness
import nz.eloque.compose_kit.input.AbbreviatingText
import nz.eloque.compose_kit.pager.HorizontalPagerIndicator
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.model.field.PassField
import nz.eloque.foss_wallet.persistence.BarcodePosition
import nz.eloque.foss_wallet.ui.card.PassField
import java.io.File

@Composable
fun Barcodes(
    barcodes: List<BarCode>,
    legacyRendering: Boolean,
    barcodePosition: BarcodePosition,
    increaseBrightness: Boolean,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState { barcodes.size }

    if (increaseBrightness) UpdateBrightness()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 16.dp,
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Barcode(
                    barcode = barcodes[it],
                    legacyRendering = legacyRendering,
                    barcodePosition = barcodePosition,
                )
            }
        }

        if (barcodes.size > 1) {
            HorizontalPagerIndicator(
                pagerState = pagerState,
                activeColor = LocalContentColor.current,
                inactiveColor = LocalContentColor.current.copy(alpha = 0.3f),
            )
        }
    }
}

@Composable
fun Barcode(
    barcode: BarCode,
    modifier: Modifier = Modifier,
    legacyRendering: Boolean = false,
    barcodePosition: BarcodePosition = BarcodePosition.Center,
) {
    val barcodeBitmap = barcode.toBitmap(legacyRendering = legacyRendering)

    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(5.dp))
                .background(Color.White)
                .sizeIn(maxWidth = 320.dp, maxHeight = 260.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (barcodeBitmap != null) {
            var showFullscreen by remember { mutableStateOf(false) }
            val scaledWidth = (2.5.dp * barcodeBitmap.width).coerceIn(125.dp, 300.dp)
            val isLinearBarcode = barcodeBitmap.height == 1
            val aspectRatio =
                if (isLinearBarcode) {
                    scaledWidth / 90.dp
                } else {
                    barcodeBitmap.width.toFloat() / barcodeBitmap.height
                }

            Image(
                bitmap = barcodeBitmap.asImageBitmap(),
                contentDescription = stringResource(R.string.barcode),
                modifier =
                    Modifier
                        .padding(10.dp)
                        .padding(horizontal = if (isLinearBarcode) 10.dp else 0.dp)
                        .widthIn(max = scaledWidth)
                        .aspectRatio(aspectRatio)
                        .weight(1f, fill = false)
                        .clickable { showFullscreen = true },
                contentScale = ContentScale.FillBounds,
                filterQuality = FilterQuality.None,
            )

            if (showFullscreen) {
                FullscreenDialog(
                    onDismiss = { showFullscreen = false },
                    contentAlignment = barcodePosition.alignment,
                ) {
                    Image(
                        bitmap = barcodeBitmap.asImageBitmap(),
                        contentDescription = stringResource(R.string.barcode),
                        modifier =
                            Modifier
                                .background(Color.White)
                                .padding(24.dp)
                                .aspectRatio(aspectRatio),
                        contentScale = ContentScale.FillBounds,
                        filterQuality = FilterQuality.None,
                    )
                }
            }
        } else {
            BrokenBarcodeWarning()
        }

        barcode.altText?.let {
            AbbreviatingText(
                text = it,
                modifier = Modifier.padding(horizontal = 10.dp).padding(bottom = 4.dp),
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun BrokenBarcodeWarning() {
    Icon(
        imageVector = Icons.Default.WarningAmber,
        contentDescription = null,
        modifier = Modifier.padding(48.dp).size(48.dp),
        tint = Color.Red,
    )
}

@Composable
fun AsyncPassImage(
    model: File?,
    modifier: Modifier = Modifier,
) {
    model?.let {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            AsyncImage(
                model = it,
                contentDescription = stringResource(R.string.image),
                contentScale = ContentScale.FillWidth,
                modifier = modifier,
            )
        }
    }
}

@Composable
fun BackFields(fields: List<PassField>) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        fields.forEach {
            PassField(
                field = it,
                labelColor = Color.Unspecified,
                maxLines = Int.MAX_VALUE,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
