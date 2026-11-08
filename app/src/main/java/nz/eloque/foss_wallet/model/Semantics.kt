package nz.eloque.foss_wallet.model

import nz.eloque.foss_wallet.parsing.TimeParser
import nz.eloque.foss_wallet.utils.stringOrNull
import org.json.JSONException
import org.json.JSONObject
import java.time.Duration
import java.time.ZonedDateTime

/** The pass-level semantic tags the app uses, read from [Pass.semantics]. */
data class Semantics(
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
    val departureDate: ZonedDateTime? get() = currentDepartureDate ?: originalDepartureDate

    val arrivalDate: ZonedDateTime? get() = currentArrivalDate ?: originalArrivalDate

    companion object {
        fun fromJson(json: String?): Semantics? {
            val semantics =
                try {
                    JSONObject(json ?: return null)
                } catch (_: JSONException) {
                    return null
                }
            return Semantics(
                eventName = semantics.stringOrNull("eventName"),
                eventStartDate = semantics.dateOrNull("eventStartDate"),
                eventEndDate = semantics.dateOrNull("eventEndDate"),
                duration = semantics.optDouble("duration").takeIf { it.isFinite() && it > 0 }?.let { Duration.ofSeconds(it.toLong()) },
                venueName = semantics.stringOrNull("venueName"),
                currentDepartureDate = semantics.dateOrNull("currentDepartureDate"),
                originalDepartureDate = semantics.dateOrNull("originalDepartureDate"),
                currentArrivalDate = semantics.dateOrNull("currentArrivalDate"),
                originalArrivalDate = semantics.dateOrNull("originalArrivalDate"),
                departureLocationDescription = semantics.stringOrNull("departureLocationDescription"),
                departureStationName = semantics.stringOrNull("departureStationName"),
                departureAirportName = semantics.stringOrNull("departureAirportName"),
            )
        }

        private fun JSONObject.dateOrNull(key: String): ZonedDateTime? = stringOrNull(key)?.let { TimeParser.parseAbsoluteOrNull(it) }
    }
}

fun Pass.parsedSemantics(): Semantics? = Semantics.fromJson(semantics)
