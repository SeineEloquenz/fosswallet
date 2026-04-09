package nz.eloque.foss_wallet.ui.card.primary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.TransitType
import nz.eloque.foss_wallet.model.field.PassField
import nz.eloque.foss_wallet.ui.card.AutoSizePassFields
import nz.eloque.foss_wallet.ui.card.PassField

@Composable
fun BoardingPrimary(
    pass: Pass,
    transitType: TransitType,
    isSelectable: Boolean = true
) {
    val departureField = pass.primaryFields.getOrElse(0) { PassField.Empty }
    val destinationField = pass.primaryFields.getOrElse(1) { PassField.Empty }

    val iconWidth = 40.dp
    val space = 10.dp

    AutoSizePassFields(
        fields = listOf(departureField, destinationField),
        modifier = Modifier.height(70.dp),
        spacing = iconWidth + space * 2,
        useFixedWidth = true
    ) { fontSize ->
        Row(
            horizontalArrangement = Arrangement.spacedBy(space),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PassField(
                field = departureField,
                modifier = Modifier.weight(1f),
                fontSize = fontSize,
                isSelectable = isSelectable
            )

            Column {
                //The text correctly spaces the icon
                Text("", style = MaterialTheme.typography.labelMedium)
                Icon(
                    imageVector = transitType.icon,
                    contentDescription = stringResource(R.string.to),
                    modifier = Modifier.width(iconWidth).fillMaxHeight()
                )
            }

            PassField(
                field = destinationField,
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
                fontSize = fontSize,
                isSelectable = isSelectable
            )
        }
    }
}
