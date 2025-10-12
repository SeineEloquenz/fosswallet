package nz.eloque.foss_wallet.ui.screens.pass

import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.model.field.PassField
import nz.eloque.foss_wallet.persistence.BarcodePosition
import nz.eloque.foss_wallet.ui.card.LabelAlign
import nz.eloque.foss_wallet.ui.card.OutlinedPassLabel
import nz.eloque.foss_wallet.ui.card.PlainPassLabel
import nz.eloque.foss_wallet.ui.components.Raise
import nz.eloque.foss_wallet.ui.effects.UpdateBrightness
import java.io.File


@Composable
fun HeaderFieldsView(
    headerFields: List<PassField>
) {
    Row(
        modifier = Modifier.wrapContentWidth(Alignment.End)
    ) {
        headerFields.forEach { PlainPassLabel(
            label = it.label,
            content = it.content,
            labelAlign = LabelAlign.RIGHT,
        ) }
    }
}

@Composable
fun BarcodesView(
    legacyRendering: Boolean,
    barcode: BarCode,
    barcodePosition: BarcodePosition,
    increaseBrightness: Boolean,
) {
    var fullscreen by remember { mutableStateOf(false) }
    if (increaseBrightness) {
        UpdateBrightness()
    }
    val image = barcode.encodeAsBitmap(1000, 1000, legacyRendering)

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = stringResource(R.string.image),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .heightIn(max = 150.dp)
                    .widthIn(max = 150.dp)
                    .clickable { fullscreen = !fullscreen }
            )
            barcode.altText?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(180.dp)
                )
            }
        }
    }

    if (fullscreen) {
        Raise(onDismiss = { fullscreen = !fullscreen }) {
            UpdateBrightness()
            PassImage(
                bitmap = image,
                barcodePosition = barcodePosition,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun PassImage(
    bitmap: Bitmap?,
    modifier: Modifier = Modifier,
    barcodePosition: BarcodePosition
) {
    bitmap?.let {
        Column(
            verticalArrangement = barcodePosition.arrangement,
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = stringResource(R.string.image),
                contentScale = ContentScale.Fit,
                modifier = modifier
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun AsyncPassImage(
    model: File?,
    modifier: Modifier = Modifier
) {
    model?.let {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            AsyncImage(
                model = it,
                contentDescription = stringResource(R.string.image),
                contentScale = ContentScale.Fit,
                modifier = modifier
            )
        }
    }
}

@Composable
fun BackFields(
    fields: List<PassField>,
    modifier: Modifier = Modifier,
    cardColors: CardColors = CardDefaults.outlinedCardColors()
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = modifier
    ) {
        fields.forEach {
            OutlinedPassLabel(
                label = it.label,
                content = it.content,
                modifier = Modifier.fillMaxWidth(),
                colors = cardColors
            )
        }
    }
}