package nz.eloque.foss_wallet.ui.components.pass_view

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.model.PassField
import nz.eloque.foss_wallet.ui.components.PassLabel
import nz.eloque.foss_wallet.ui.components.Raise
import nz.eloque.foss_wallet.ui.components.UpdateBrightness
import java.io.File


@Composable
fun HeaderFieldsView(
    cardColors: CardColors,
    headerFields: List<PassField>
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        for (field in headerFields) {
            PassLabel(
                label = field.label,
                content = field.value,
                colors = cardColors,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun BarcodesView(
    barcodes: Set<BarCode>
) {
    val fullscreen = remember { mutableStateOf(false) }
    barcodes.firstOrNull()?.let {
        val image = it.encodeAsBitmap(1000, 1000)
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            PassImage(
                bitmap = image,
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp)
                    .clickable { fullscreen.value = !fullscreen.value }
            )
        }
        if (fullscreen.value) {
            Raise(onDismiss = { fullscreen.value = !fullscreen.value }) {
                UpdateBrightness()
                PassImage(image)
            }
        }
    }
}

@Composable
fun PassImage(
    bitmap: Bitmap?,
    modifier: Modifier = Modifier
) {
    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = stringResource(R.string.image),
            contentScale = ContentScale.Fit,
            modifier = modifier
                .fillMaxWidth()
        )
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
                .clip(RoundedCornerShape(15.dp))
        )
    }
}

@Composable
fun PassField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    cardColors: CardColors = CardDefaults.outlinedCardColors()
) {
    PassLabel(
        label = label,
        content = value,
        modifier = modifier,
        cardColors
    )
}

@Composable
fun PassFields(
    fields: List<PassField>,
    modifier: Modifier = Modifier,
    cardColors: CardColors = CardDefaults.outlinedCardColors()
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = modifier
    ) {
        fields.forEach {
            PassField(it.label, it.value, Modifier.fillMaxWidth(), cardColors)
        }
    }
}