package nz.eloque.foss_wallet.parsing

import nz.eloque.foss_wallet.model.field.PassDateTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

/**
 * Parses the date format the pkpass standard mandates: the W3C datetime profile of ISO 8601
 * (https://www.w3.org/TR/NOTE-datetime), which Apple's docs refer to as a "W3C timestamp".
 *
 * The profile admits more shapes than [ZonedDateTime.parse] accepts on its own:
 *
 *   2013-04-24T10:00-05:00        complete date plus hours and minutes  (canonical)
 *   2013-04-24T10:00:00-05:00     plus seconds
 *   2013-04-24T10:00:00.123Z      plus fractional seconds
 *   2013-04-24                    date only
 *
 * On top of that, generators in the wild routinely emit values with no offset at all
 * (`2013-04-24T10:00:00`). Passing any of the last two forms to [ZonedDateTime.parse] throws, and
 * in [FieldParser] that exception escaped as far as the pass importer, so a single malformed field
 * failed the import of the whole pass. Values without an offset are inherently wall-clock readings,
 * so they parse to [PassDateTime.Floating].
 */
object TimeParser {
    /** @throws DateTimeParseException if [value] matches none of the accepted shapes. */
    fun parse(value: String): PassDateTime {
        val trimmed = value.trim()
        return parseOrNull(trimmed)
            ?: throw DateTimeParseException("Not a W3C timestamp: $trimmed", trimmed, 0)
    }

    fun parseOrNull(value: String): PassDateTime? {
        val trimmed = value.trim()
        return trimmed.asZoned()?.let { PassDateTime.Absolute(it) }
            ?: trimmed.asLocalDateTime()?.let { PassDateTime.Floating(it) }
            ?: trimmed.asLocalDate()?.let { PassDateTime.Floating(it.atStartOfDay()) }
    }

    /**
     * Parses a value that the standard defines as an instant rather than a field to render:
     * `relevantDate`, `relevantDates` entries and `expirationDate`. These carry no
     * `ignoresTimeZone` companion, so an offsetless value is anchored in [zone] rather than
     * discarded — dropping it would silently disable expiry and relevance for the pass.
     */
    fun parseAbsoluteOrNull(
        value: String,
        zone: ZoneId = ZoneId.systemDefault(),
    ): ZonedDateTime? =
        when (val parsed = parseOrNull(value)) {
            is PassDateTime.Absolute -> parsed.value
            is PassDateTime.Floating -> parsed.value.atZone(zone)
            null -> null
        }

    private fun String.asZoned(): ZonedDateTime? =
        try {
            ZonedDateTime.parse(this)
        } catch (_: DateTimeParseException) {
            null
        }

    private fun String.asLocalDateTime(): LocalDateTime? =
        try {
            LocalDateTime.parse(this)
        } catch (_: DateTimeParseException) {
            null
        }

    private fun String.asLocalDate(): LocalDate? =
        try {
            LocalDate.parse(this)
        } catch (_: DateTimeParseException) {
            null
        }
}
