package nz.eloque.foss_wallet.ui.components.pass_view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass

@Composable
fun BoardingPassView(
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
        PassTopBar(pass)
        Divider()
        AsyncPassImage(model = pass.stripFile(context), modifier = Modifier.fillMaxWidth())
        Column(
            verticalArrangement = Arrangement.spacedBy(25.dp),
            modifier = Modifier
                .padding(10.dp)
        ) {
            if (showFront) {
                if (pass.primaryFields.size == 1) {
                    val field = pass.primaryFields[0]
                    PassField(field.label, field.value)
                    Divider()
                } else if (pass.primaryFields.size >= 2) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DestinationCard(
                            destination = pass.primaryFields[0].value,
                            modifier = Modifier.weight(2f)
                        )
                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = stringResource(R.string.to),
                            modifier = Modifier.weight(1f)
                        )
                        DestinationCard(
                            destination = pass.primaryFields[1].value,
                            modifier = Modifier.weight(2f)
                        )
                    }
                    Divider()
                }
                if (pass.auxiliaryFields.isNotEmpty()) {
                    PassFields(pass.auxiliaryFields)
                    Divider()
                }
                if (pass.secondaryFields.isNotEmpty()) {
                    PassFields(pass.secondaryFields)
                    Divider()
                }
                BarcodesView(pass.barCodes)
            } else {
                PassField(stringResource(R.string.serial_number), pass.serialNumber)
                PassField(stringResource(R.string.organization), pass.organization)
                PassFields(pass.backFields)
            }
        }
        AsyncPassImage(model = pass.logoFile(context), modifier = Modifier.fillMaxWidth())
        AsyncPassImage(model = pass.footerFile(context), modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun DestinationCard(
    destination: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Text(
            text = destination,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(5.dp)
        )
    }
}