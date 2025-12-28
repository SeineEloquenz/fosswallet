package nz.eloque.foss_wallet.ui.card

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import nz.eloque.foss_wallet.model.LocalizedPassWithTags
import nz.eloque.foss_wallet.model.PassRelevantDate
import nz.eloque.foss_wallet.ui.components.CalendarButton
import nz.eloque.foss_wallet.ui.components.LocationButton


@Composable
fun PassCardFooter(
    localizedPass: LocalizedPassWithTags,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        val pass = localizedPass.pass
        val tags = localizedPass.tags

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
    }
}