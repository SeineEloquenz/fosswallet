package nz.eloque.foss_wallet.model

import java.time.ZonedDateTime

data class Timeframe(
    val start: ZonedDateTime?,
    val end: ZonedDateTime?,
    val location: String? = null,
)

/** The event or journey of this pass, from its semantic tags or else its first relevant date. */
fun Pass.timeframe(): Timeframe? {
    semantics?.timeframe()?.let { return it }
    relevantDates.filterIsInstance<PassRelevantDate.DateInterval>().firstOrNull()?.let {
        return Timeframe(it.startDate, it.endDate)
    }
    return relevantDates.filterIsInstance<PassRelevantDate.Date>().firstOrNull()?.let {
        Timeframe(it.date, expirationDate)
    }
}
