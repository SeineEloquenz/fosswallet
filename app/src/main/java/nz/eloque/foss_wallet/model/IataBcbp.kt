package nz.eloque.foss_wallet.model

import com.google.zxing.BarcodeFormat
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlin.math.abs

object IataBcbp {

    data class Parsed(
        val passengerName: String,
        val fromAirport: String,
        val toAirport: String,
        val carrierCode: String,
        val flightNumber: String,
        val flightDate: LocalDate?,
        val travelClass: String,
        val seat: String,
        val pnr: String,
        val checkInSequence: String,
        val passengerStatus: String,
    ) {
        fun flightCode(): String {
            val normalizedFlight = flightNumber.trimStart('0')
            return "$carrierCode${normalizedFlight.ifBlank { flightNumber }}"
        }

        fun summary(): String {
            return "$fromAirport->$toAirport | ${flightCode()} | ${seatLabel()}"
        }

        private fun seatLabel(): String = if (seat.isNotBlank()) "Seat $seat" else "Seat -"
    }

    private val supportedFormats = setOf(
        BarcodeFormat.AZTEC,
        BarcodeFormat.PDF_417,
        BarcodeFormat.QR_CODE,
    )

    fun isBcbp(format: BarcodeFormat, rawMessage: String): Boolean {
        return parse(format, rawMessage) != null
    }

    fun parse(format: BarcodeFormat, rawMessage: String): Parsed? {
        if (format !in supportedFormats) return null

        val message = normalize(rawMessage)
        if (message.length < 60) return null
        if (message.firstOrNull() != 'M') return null
        if (message[1] !in '1'..'9') return null

        val passengerNameRaw = message.substring(2, 22)
        if (!passengerNameRaw.all { it.isUpperCase() || it.isDigit() || it == ' ' || it == '/' }) return null

        val from = message.sliceRangeTrimmed(30, 33)
        val to = message.sliceRangeTrimmed(33, 36)
        val carrier = message.sliceRangeTrimmed(36, 39)
        val flight = message.sliceRangeTrimmed(39, 44)
        if (from.length != 3 || to.length != 3 || carrier.length !in 2..3 || flight.isBlank()) return null

        val dayOfYear = message.sliceRangeTrimmed(44, 47).toIntOrNull()
        val seat = message.sliceRangeTrimmed(48, 52).normalizePaddedNumberWithOptionalSuffix()

        return Parsed(
            passengerName = passengerNameRaw.prettyPassengerName(),
            fromAirport = from,
            toAirport = to,
            carrierCode = carrier,
            flightNumber = flight,
            flightDate = dayOfYear?.let { inferDate(it) },
            travelClass = message.sliceRangeTrimmed(47, 48),
            seat = seat,
            pnr = message.sliceRangeTrimmed(23, 30),
            checkInSequence = message.sliceRangeTrimmed(52, 57).normalizePaddedNumberWithOptionalSuffix(),
            passengerStatus = message.sliceRangeTrimmed(57, 58),
        )
    }

    private fun normalize(rawMessage: String): String {
        val trimmed = rawMessage.trim()
        return if (trimmed.length > 3 && trimmed[0] == ']') {
            trimmed.drop(3).trimStart()
        } else {
            trimmed
        }
    }

    private fun String.sliceRangeTrimmed(startInclusive: Int, endExclusive: Int): String {
        if (length < endExclusive) return ""
        return substring(startInclusive, endExclusive).trim()
    }

    private fun String.prettyPassengerName(): String {
        val normalized = trim().replace(Regex("\\s+"), " ")
        val split = normalized.split("/", limit = 2)
        val lastName = split.getOrNull(0)?.trim().orEmpty().titleCaseWords()
        val firstName = split.getOrNull(1)?.trim().orEmpty().titleCaseWords()
        return listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ").ifBlank { normalized }
    }

    private fun String.titleCaseWords(): String {
        if (isBlank()) return ""
        return lowercase().split(' ').joinToString(" ") { word ->
            word.split('-').joinToString("-") { piece ->
                piece.replaceFirstChar { c -> c.titlecase() }
            }
        }
    }

    private fun String.normalizePaddedNumberWithOptionalSuffix(): String {
        val trimmed = trim()
        if (trimmed.isBlank()) return ""
        val match = Regex("^(0*)(\\d+)([A-Z]?)$").find(trimmed) ?: return trimmed
        val number = match.groupValues[2].trimStart('0').ifBlank { "0" }
        return number + match.groupValues[3]
    }

    private fun inferDate(dayOfYear: Int): LocalDate? {
        if (dayOfYear !in 1..366) return null
        val now = LocalDate.now(ZoneOffset.UTC)
        return (now.year - 1..now.year + 1)
            .mapNotNull { year ->
                runCatching { LocalDate.ofYearDay(year, dayOfYear) }.getOrNull()
            }
            .minByOrNull { candidate ->
                abs(ChronoUnit.DAYS.between(now, candidate))
            }
    }
}
