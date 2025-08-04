package nz.eloque.foss_wallet.ui.view.pass

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    barcodes: Set<BarCode>,
    barcodePosition: BarcodePosition,
    increaseBrightness: Boolean,
) {
    val fullscreen = remember { mutableStateOf(false) }
    if (increaseBrightness) {
        UpdateBrightness()
    }
    barcodes.firstOrNull()?.let {
        val image = it.encodeAsBitmap(1000, 1000)
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PassImage(
                    bitmap = image,
                    modifier = Modifier
                        .heightIn(max = 150.dp)
                        .fillMaxWidth()
                        .clickable { fullscreen.value = !fullscreen.value },
                    barcodePosition = barcodePosition
                )
                it.altText?.let { Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(180.dp)
                ) }
            }
        }
        if (fullscreen.value) {
            Raise(onDismiss = { fullscreen.value = !fullscreen.value }) {
                UpdateBrightness()
                PassImage(
                    bitmap = image,
                    barcodePosition = barcodePosition,
                    modifier = Modifier.padding(16.dp)
                )
            }
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
        AsyncImage(
            model = it,
            contentDescription = stringResource(R.string.image),
            contentScale = ContentScale.Fit,
            modifier = modifier
                .width(100.dp)
                .height(100.dp)
        )
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