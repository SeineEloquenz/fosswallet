package nz.eloque.foss_wallet.ui.screens.pass

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import coil.compose.AsyncImage
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.model.field.PassField
import nz.eloque.foss_wallet.persistence.BarcodePosition
import nz.eloque.foss_wallet.ui.card.PassField
import nz.eloque.foss_wallet.ui.components.FullscreenBarcode
import nz.eloque.foss_wallet.ui.effects.UpdateBrightness
import java.io.File

@Composable
fun BarcodesView(
    legacyRendering: Boolean,
    barcodes: List<BarCode>,
    barcodePosition: BarcodePosition,
    increaseBrightness: Boolean,
) {
    var fullscreenIndex by remember { mutableStateOf<Int?>(null) }
    val pagerState =
        rememberPagerState(
            initialPage = 0,
            pageCount = { barcodes.size },
        )
    if (increaseBrightness) {
        UpdateBrightness()
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.White)
                    .padding(10.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                HorizontalPager(
                    state = pagerState,
                    pageSpacing = 10.dp,
                    modifier =
                        Modifier
                            .widthIn(max = if (barcodes.any { it.is1d() }) 320.dp else 170.dp),
                ) { index ->
                    val barcode = barcodes[index]
                    val image =
                        barcode.encodeAsBitmap(
                            if (barcode.is1d()) 3000 else 1000,
                            1000,
                            legacyRendering,
                        )
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (image != null) {
                            Image(
                                bitmap = image.asImageBitmap(),
                                contentDescription = stringResource(R.string.image),
                                contentScale = ContentScale.Fit,
                                modifier =
                                    Modifier
                                        .heightIn(max = 150.dp)
                                        .widthIn(max = if (barcode.is1d()) 300.dp else 150.dp)
                                        .clickable { fullscreenIndex = index },
                            )
                        } else {
                            BrokenBarcodeWarning()
                        }
                        barcode.altText?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = it,
                                color = Color.Black,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(150.dp),
                            )
                        }
                    }
                }
                if (barcodes.size > 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    BarcodePagerIndicator(
                        selectedItem = pagerState.currentPage,
                        itemCount = barcodes.size,
                    )
                }
            }
        }
    }

    fullscreenIndex?.let { index ->
        val fullscreenBarcode = barcodes.getOrNull(index) ?: return@let
        val fullscreenImage =
            fullscreenBarcode.encodeAsBitmap(
                if (fullscreenBarcode.is1d()) 3000 else 1000,
                1000,
                legacyRendering,
            )
        if (fullscreenImage != null) {
            FullscreenBarcode(
                image = fullscreenImage,
                barcodePosition = barcodePosition,
                isFullscreen = true,
                onDismiss = { fullscreenIndex = null },
            )
        } else {
            BrokenBarcodeWarning()
        }
    }
}

@Composable
private fun BrokenBarcodeWarning() {
    Box(
        modifier =
            Modifier
                .heightIn(max = 150.dp)
                .widthIn(max = 150.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "⚠",
            color = MaterialTheme.colorScheme.error,
            fontSize = 16.em,
        )
    }
}

@Composable
private fun BarcodePagerIndicator(
    selectedItem: Int,
    itemCount: Int,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(itemCount) { index ->
            val isSelected = index == selectedItem
            Box(
                modifier =
                    Modifier
                        .padding(4.dp)
                        .width(if (isSelected) 14.dp else 8.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f)),
            )
        }
    }
}

@Composable
fun PassImage(
    bitmap: Bitmap?,
    modifier: Modifier = Modifier,
    barcodePosition: BarcodePosition,
) {
    bitmap?.let {
        Column(
            verticalArrangement = barcodePosition.arrangement,
            modifier = Modifier.fillMaxSize(),
        ) {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = stringResource(R.string.image),
                contentScale = ContentScale.Fit,
                modifier =
                    modifier
                        .fillMaxWidth(),
            )
        }
    }
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
                maxLines = Int.MAX_VALUE,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
