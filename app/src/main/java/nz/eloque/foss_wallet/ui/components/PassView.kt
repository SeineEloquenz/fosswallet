package nz.eloque.foss_wallet.ui.components

import android.content.res.Resources
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
        Column {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .weight(5f)
                ) {
                    Text(
                        text = pass.description,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .padding(5.dp)
                    )
                    HeaderContent("placeholder")
                }
                Image(
                    bitmap = pass.icon.asImageBitmap(),
                    contentDescription = stringResource(R.string.image),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .padding(5.dp)
                        .align(Alignment.CenterVertically)
                        .weight(2f)
                )
            }
            HeaderContent(pass.serialNumber)
            HeaderContent(pass.organization)
        }
        PassImage(pass.strip)
        var tabIndex by remember { mutableIntStateOf(0) }
        val tabs = listOf("Front", "Back")
        TabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = tabIndex == index,
                    onClick = { tabIndex = index }
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(25.dp),
            modifier = Modifier
                .padding(10.dp)
        ) {
            when (tabIndex) {
                0 -> {
                    PassImage(pass.barCode?.encodeAsBitmap(250, 250))
                    PassFields(pass.primaryFields)
                    PassFields(pass.secondaryFields)
                    PassFields(pass.auxiliaryFields)
                }
                1 -> {
                    PassFields(pass.backFields)
                }
            }
        }
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
    field: PassField,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (field.label.length + field.value.length <= 25) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = field.label,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(8.dp)
                )
                Spacer(modifier.weight(1f))
                Text(
                    text = field.value,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(8.dp)
                )
            }
        } else {
            Column(
            ) {
                Text(
                    text = field.label,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(8.dp)
                )
                Text(
                    text = field.value,
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
            PassField(it)
        }
    }
}