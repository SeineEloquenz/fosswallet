package nz.eloque.foss_wallet.model

import java.time.ZonedDateTime

sealed class PassRelevantDate {
    data class Date (val date: ZonedDateTime) : PassRelevantDate()
    data class DateInterval (val startDate: ZonedDateTime, val endDate: ZonedDateTime) : PassRelevantDate()
}
