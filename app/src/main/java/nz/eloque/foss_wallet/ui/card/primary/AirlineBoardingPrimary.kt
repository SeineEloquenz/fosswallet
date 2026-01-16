package nz.eloque.foss_wallet.ui.card.primary

import android.widget.Toast
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.TransitType
import nz.eloque.foss_wallet.model.field.PassContent
import nz.eloque.foss_wallet.ui.card.OutlinedPassLabel
import nz.eloque.foss_wallet.ui.components.AbbreviatingText
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
        OutlinedPassLabel(
            label = field.label,
            content = field.content,
            modifier = modifier,
            colors = cardColors
        )
    } else if (pass.primaryFields.size >= 2) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(horizontal = 8.dp),
        ) {
            DestinationCard(
                label = pass.primaryFields[0].label,
                destination = pass.primaryFields[0].content,
                modifier = Modifier.weight(2f),
                cardColors
            )
            Icon(
                imageVector = transitType.icon,
                contentDescription = stringResource(R.string.to),
                modifier = Modifier.weight(1f)
            )
            DestinationCard(
                label = pass.primaryFields[1].label,
                destination = pass.primaryFields[1].content,
                modifier = Modifier.weight(2f),
                cardColors
            )
        }
    }
}

@Composable
private fun DestinationCard(
    label: String?,
    destination: PassContent,
    modifier: Modifier = Modifier,
    cardColors: CardColors
) {
    ElevatedCard(
        colors = cardColors.copy(containerColor = cardColors.containerColor.darken(0.75f)),
        modifier = modifier
    ) {
        val context = LocalContext.current

        val destination = destination.prettyPrint()
        val isCode = destination.length <= 3
        AbbreviatingText(
            text = destination,
            style = if (isCode) { MaterialTheme.typography.headlineLarge } else { MaterialTheme.typography.headlineSmall },
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(5.dp)
                .align(Alignment.CenterHorizontally)
                .combinedClickable(
                    onClick = {},
                    onLongClick = if (isCode && label != null) {
                        {
                            Toast.makeText(context, label, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        {}
                    }
                ),
        )
    }
}