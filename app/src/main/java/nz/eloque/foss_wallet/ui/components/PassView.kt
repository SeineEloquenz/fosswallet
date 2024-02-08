package nz.eloque.foss_wallet.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import java.io.File

@Composable
fun PassView(
    pass: Pass,
    showFront: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.End),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = pass.description,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .weight(5f)
                )
                AsyncImage(
                    model = pass.passIdent,
                    contentDescription = "",
                )
                AsyncImage(
                    model = (pass.thumbnailFile(context) ?: pass.iconFile(context)),
                    contentDescription = stringResource(R.string.image),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(2f)
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DateView(pass.description, pass.relevantDate, pass.expirationDate)
                pass.locations.firstOrNull()?.let { LocationButton(it) }
            }
            HeaderContent(pass.passIdent)
            HeaderContent(pass.webServiceUrl)
            HeaderContent(pass.authToken)
        }
        Divider()
        AsyncPassImage(pass.stripFile(context))
        Column(
            verticalArrangement = Arrangement.spacedBy(25.dp),
            modifier = Modifier
                .padding(10.dp)
        ) {
            if (showFront) {
                BarcodesView(pass.barCodes)
                PassFields(pass.primaryFields)
                PassFields(pass.secondaryFields)
                PassFields(pass.auxiliaryFields)
            } else {
                PassField(stringResource(R.string.serial_number), pass.serialNumber)
                PassField(stringResource(R.string.organization), pass.organization)
                PassFields(pass.backFields)
            }
        }
        AsyncPassImage(pass.logoFile(context))
        AsyncPassImage(pass.footerFile(context))
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
            modifier = Modifier
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
        )
    }
}

@Composable
fun HeaderContent(
    value: String?,
    modifier: Modifier = Modifier
) {
    value?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(5.dp)
        )
    }
}

@Composable
fun PassField(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (label.length + value.length <= 25) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(8.dp)
                )
                Spacer(modifier.weight(1f))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(8.dp)
                )
            }
        } else {
            Column(
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(8.dp)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun PassFields(
    fields: List<PassField>,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        fields.forEach {
            PassField(it.label, it.value)
        }
    }
}