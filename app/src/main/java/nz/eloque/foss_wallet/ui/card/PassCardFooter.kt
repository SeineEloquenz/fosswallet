package nz.eloque.foss_wallet.ui.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.LocalizedPassWithTags
import nz.eloque.foss_wallet.model.PassRelevantDate
import nz.eloque.foss_wallet.model.Tag
import nz.eloque.foss_wallet.ui.components.CalendarButton
import nz.eloque.foss_wallet.ui.components.ChipRow
import nz.eloque.foss_wallet.ui.components.LocationButton


@Composable
fun PassCardFooter(
    localizedPass: LocalizedPassWithTags,
    allTags: Set<Tag>,
    onTagClick: (Tag) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        val pass = localizedPass.pass
        val tags = localizedPass.tags

        Spacer(modifier = Modifier.width(8.dp))

        if (pass.relevantDates.any { it is PassRelevantDate.DateInterval }) {
            val interval: PassRelevantDate.DateInterval = pass.relevantDates.filter {
                it is PassRelevantDate.DateInterval
            }[0] as PassRelevantDate.DateInterval
            CalendarButton(
                title = pass.description,
                start = interval.startDate,
                end = interval.endDate
            )
        } else if (pass.relevantDates.any { it is PassRelevantDate.Date }) {
            val date: PassRelevantDate.Date = pass.relevantDates.filter {
                it is PassRelevantDate.Date
            }[0] as PassRelevantDate.Date
            CalendarButton(
                title = pass.description,
                start = date.date,
                end = pass.expirationDate
            )
        }
        pass.locations.firstOrNull()?.let { LocationButton(it) }

        ChipRow(
            options = tags,
            onOptionClick = { onTagClick(it) },
            optionLabel = { it.label },
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = {

        }) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_tag)
            )
        }
    }
}