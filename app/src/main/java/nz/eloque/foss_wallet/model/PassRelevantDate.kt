package nz.eloque.foss_wallet.model

import java.time.ZonedDateTime

sealed class PassRelevantDate {
    data class Date (val date: ZonedDateTime) : PassRelevantDate() {
        override fun startDate(): ZonedDateTime = date
    }

    data class DateInterval (val startDate: ZonedDateTime, val endDate: ZonedDateTime) : PassRelevantDate() {
        override fun startDate(): ZonedDateTime = startDate
    }

    abstract fun startDate(): ZonedDateTime
}
