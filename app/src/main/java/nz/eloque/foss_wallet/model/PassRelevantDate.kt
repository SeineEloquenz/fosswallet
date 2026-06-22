package nz.eloque.foss_wallet.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime

@Serializable
sealed class PassRelevantDate {
    @Serializable
    @SerialName("date")
    data class Date(
        @Contextual val date: ZonedDateTime,
    ) : PassRelevantDate() {
        override fun startDate(): ZonedDateTime = date
    }

    @Serializable
    @SerialName("interval")
    data class DateInterval(
        @Contextual val startDate: ZonedDateTime,
        @Contextual val endDate: ZonedDateTime,
    ) : PassRelevantDate() {
        override fun startDate(): ZonedDateTime = startDate
    }

    abstract fun startDate(): ZonedDateTime
}
