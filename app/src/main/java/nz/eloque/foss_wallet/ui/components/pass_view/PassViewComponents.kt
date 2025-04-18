package nz.eloque.foss_wallet.ui.components.pass_view

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassField
import nz.eloque.foss_wallet.ui.components.DateView
import nz.eloque.foss_wallet.ui.components.LocationButton
import nz.eloque.foss_wallet.ui.components.PassLabel
import nz.eloque.foss_wallet.ui.components.Raise
import nz.eloque.foss_wallet.ui.components.UpdateBrightness
import java.io.File


@Composable
fun PassTopBar(
    pass: Pass,
) {
    val context = LocalContext.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.Start),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .weight(5f)
        ) {
            Text(
                text = pass.logoText ?: pass.description,
                style = MaterialTheme.typography.headlineSmall,
            )
            HeaderFieldsView(
                headerFields = pass.headerFields,
                cardColors = CardDefaults.outlinedCardColors()
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DateView(pass.description, pass.relevantDate, pass.expirationDate)
                pass.locations.firstOrNull()?.let { LocationButton(it) }
            }
        }
        Spacer(modifier = Modifier.width(5.dp))
        AsyncPassImage(
            model = (pass.thumbnailFile(context) ?: pass.iconFile(context)),
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(2f)
                .width(100.dp)
                .height(100.dp)
        )
    }
}

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
        PassImage(bitmap = image, modifier = Modifier.clickable { fullscreen.value = !fullscreen.value })
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
            contentScale = ContentScale.FillBounds,
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
    modifier: Modifier = Modifier
) {
    PassLabel(
        label = label,
        content = value,
        modifier = modifier
    )
}

@Composable
fun PassFields(
    fields: List<PassField>,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = modifier
    ) {
        fields.forEach {
            PassField(it.label, it.value, Modifier.fillMaxWidth())
        }
    }
}