package nz.eloque.foss_wallet.ui.components.card_primary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardColors
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.TransitType
import nz.eloque.foss_wallet.ui.components.AbbreviatingText
import nz.eloque.foss_wallet.ui.components.pass_view.PassField
import nz.eloque.foss_wallet.utils.darken

@Composable
fun AirlineBoardingPrimary(
    pass: Pass,
    cardColors: CardColors,
    modifier: Modifier = Modifier
) {
    val transitType = TransitType.AIR

    if (pass.primaryFields.size == 1) {
        val field = pass.primaryFields[0]
        PassField(field.label, field.value, cardColors = cardColors)
    } else if (pass.primaryFields.size >= 2) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DestinationCard(
                destination = pass.primaryFields[0].value,
                modifier = Modifier.weight(2f),
                cardColors
            )
            Icon(
                imageVector = transitType.icon,
                contentDescription = stringResource(R.string.to),
                modifier = Modifier.weight(1f)
            )
            DestinationCard(
                destination = pass.primaryFields[1].value,
                modifier = Modifier.weight(2f),
                cardColors
            )
        }
    }
}

@Composable
private fun DestinationCard(
    destination: String,
    modifier: Modifier = Modifier,
    cardColors: CardColors
) {
    ElevatedCard(
        colors = cardColors.copy(containerColor = cardColors.containerColor.darken(0.75f)),
        modifier = modifier
    ) {
        AbbreviatingText(
            text = destination,
            style = if (destination.length <= 3) { MaterialTheme.typography.headlineLarge } else { MaterialTheme.typography.headlineSmall },
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(5.dp)
                .align(Alignment.CenterHorizontally),
        )
    }
}