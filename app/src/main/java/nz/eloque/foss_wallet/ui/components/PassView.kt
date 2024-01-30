package nz.eloque.foss_wallet.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassField
import nz.eloque.foss_wallet.model.RawPass

@Composable
fun PassView(
    pass: Pass,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
        ) {
            Column {
                Text(
                    text = pass.description,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(5.dp)
                )
                Text(
                    text = "placeholder",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(5.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Image(
                bitmap = pass.icon.asImageBitmap(),
                contentDescription = "logo",
                modifier = Modifier
                    .padding(5.dp)
                    .align(Alignment.CenterVertically)
                    .weight(2f)
            )
        }
        PassImage(pass.strip)
        PassValue(pass.serialNumber)
        PassValue(pass.organization)
        PassFields(pass.primaryFields)
        PassFields(pass.secondaryFields)
        PassFields(pass.auxiliaryFields)
        PassFields(pass.backFields)
        PassImage(pass.logo)
        PassImage(pass.footer)
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
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@Composable
fun PassValue(
    value: String?,
    modifier: Modifier = Modifier
) {
    value?.let {
        Card {
            Text(
                text = it,
                modifier = Modifier
                    .padding(5.dp)
            )
        }
    }
}

@Composable
fun PassField(
    field: PassField,
    modifier: Modifier = Modifier
) {
    Card {
        Row(
            modifier = Modifier.padding(5.dp)
        ) {
            Text(field.label)
            Spacer(modifier.weight(1f))
            Text(field.value)
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
            PassField(it)
        }
    }
}