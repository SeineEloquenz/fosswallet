package nz.eloque.foss_wallet.ui.components.pass_view

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
            HeaderFieldsView(headerFields = pass.headerFields)
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DateView(pass.description, pass.relevantDate, pass.expirationDate)
                pass.locations.firstOrNull()?.let { LocationButton(it) }
            }
        }
        AsyncImage(
            model = (pass.thumbnailFile(context) ?: pass.iconFile(context)),
            contentDescription = stringResource(R.string.image),
            contentScale = ContentScale.Fit,
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
    headerFields: List<PassField>
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        for (field in headerFields) {
            OutlinedTextField(
                value = field.value,
                label = { Text(field.label) },
                readOnly = true,
                onValueChange = {},
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun BarcodesView(
    barcodes: Set<BarCode>
) {
    barcodes.forEach {
        PassImage(it.encodeAsBitmap(1000, 1000))
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
                .clip(RoundedCornerShape(5.dp))
        )
    }
}

@Composable
fun PassField(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        label = { Text(label) },
        readOnly = true,
        onValueChange = {}
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
            PassField(it.label, it.value)
        }
    }
}