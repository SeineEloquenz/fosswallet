package nz.eloque.foss_wallet.model

import nz.eloque.foss_wallet.parsing.TimeParser
import nz.eloque.foss_wallet.utils.stringOrNull
import org.json.JSONObject
import java.time.Duration
import java.time.ZonedDateTime

/** The pass-level semantic tags the app reads. */
data class Semantics(
    /** Every tag as stored, never localized. */
    val json: String,
    val eventName: String? = null,
    val eventStartDate: ZonedDateTime? = null,
    val eventEndDate: ZonedDateTime? = null,
    val duration: Duration? = null,
    val venueName: String? = null,
    val currentDepartureDate: ZonedDateTime? = null,
    val originalDepartureDate: ZonedDateTime? = null,
    val currentArrivalDate: ZonedDateTime? = null,
    val originalArrivalDate: ZonedDateTime? = null,
    val departureLocationDescription: String? = null,
    val departureStationName: String? = null,
    val departureAirportName: String? = null,
) {
    /** The event or journey the tags describe. */
    fun timeframe(): Timeframe? {
        if (eventStartDate != null || eventEndDate != null) {
            return Timeframe(
                start = eventStartDate,
                end = eventEndDate ?: eventStartDate?.plusDuration(),
                location = venueName,
            )
        }
        val departure = currentDepartureDate ?: originalDepartureDate
        val arrival = currentArrivalDate ?: departure?.plusDuration() ?: originalArrivalDate
        if (departure == null && arrival == null) return null
        return Timeframe(
            start = departure,
            end = arrival,
            location = departureStationName ?: departureAirportName ?: departureLocationDescription,
        )
    }

    private fun ZonedDateTime.plusDuration(): ZonedDateTime? = duration?.let { plus(it) }

    companion object {
        fun fromJson(json: JSONObject): Semantics =
            Semantics(
                json = json.toString(),
                eventName = json.stringOrNull("eventName"),
                eventStartDate = json.dateOrNull("eventStartDate"),
                eventEndDate = json.dateOrNull("eventEndDate"),
                duration = json.optDouble("duration").takeIf { it.isFinite() && it > 0 }?.let { Duration.ofSeconds(it.toLong()) },
                venueName = json.stringOrNull("venueName"),
                currentDepartureDate = json.dateOrNull("currentDepartureDate"),
                originalDepartureDate = json.dateOrNull("originalDepartureDate"),
                currentArrivalDate = json.dateOrNull("currentArrivalDate"),
                originalArrivalDate = json.dateOrNull("originalArrivalDate"),
                departureLocationDescription = json.stringOrNull("departureLocationDescription"),
                departureStationName = json.stringOrNull("departureStationName"),
                departureAirportName = json.stringOrNull("departureAirportName"),
            )

        private fun JSONObject.dateOrNull(key: String): ZonedDateTime? = stringOrNull(key)?.let { TimeParser.parseAbsoluteOrNull(it) }
    }
}
