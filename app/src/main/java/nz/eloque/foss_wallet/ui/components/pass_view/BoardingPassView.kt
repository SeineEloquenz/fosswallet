package nz.eloque.foss_wallet.ui.components.pass_view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassField
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.model.TransitType

@Composable
fun BoardingPassView(
    pass: Pass,
    transitType: TransitType,
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
        HorizontalDivider()
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
                    HorizontalDivider()
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
                            imageVector = transitType.icon,
                            contentDescription = stringResource(R.string.to),
                            modifier = Modifier.weight(1f)
                        )
                        DestinationCard(
                            destination = pass.primaryFields[1].value,
                            modifier = Modifier.weight(2f)
                        )
                    }
                    HorizontalDivider()
                }
                if (pass.auxiliaryFields.isNotEmpty()) {
                    PassFields(pass.auxiliaryFields)
                    HorizontalDivider()
                }
                if (pass.secondaryFields.isNotEmpty()) {
                    PassFields(pass.secondaryFields)
                    HorizontalDivider()
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
            style = if (destination.length <= 3) { MaterialTheme.typography.headlineLarge } else { MaterialTheme.typography.headlineSmall },
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(5.dp)
                .align(Alignment.CenterHorizontally),
        )
    }
}

@Preview
@Composable
private fun BoardingPassPreview() {
    val pass = Pass(
        0,
        "Lufthansa Flight to SAI",
        1,
        "Lufthansa",
        "serial",
        PassType.Boarding(TransitType.AIR),
        HashSet(),
        false,
        false,
        false,
        false
    ).also {
        it.relevantDate = 1800000000L
        it.headerFields = mutableListOf(
            PassField("gate", "Gate", "47"),
            PassField("group", "Group", "3"),
            PassField("seat", "Seat", "36E")
        )
        it.primaryFields = mutableListOf(
            PassField("from", "From", "MUC"),
            PassField("to", "To", "SAI")
        )
        it.auxiliaryFields = mutableListOf(
            PassField("gate", "Gate", "47"),
            PassField("seat", "Seat", "36E")
        )
        it.secondaryFields = mutableListOf(
            PassField("name", "Passenger Name", "Max Mustermann"),
            PassField("class", "Class", "Economy")
        )
    }
    BoardingPassView(pass, TransitType.AIR, true)
}