package nz.eloque.foss_wallet.model

import com.google.zxing.BarcodeFormat
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlin.math.abs

/**
 * IATA BCBP parser used for create-flow auto-classification and field extraction.
 *
 * Sources and references used while implementing/expanding this parser:
 * - https://github.com/anomaddev/BoardingPassKit
 * - https://raw.githubusercontent.com/anomaddev/BoardingPassKit/main/Sources/BoardingPassKit/BoardingPassDecoder.swift
 * - https://raw.githubusercontent.com/anomaddev/BoardingPassKit/main/IATA_COMPLIANCE.md
 * - https://github.com/georgesmith46/bcbp
 * - https://raw.githubusercontent.com/georgesmith46/bcbp/master/src/decode.ts
 * - https://raw.githubusercontent.com/georgesmith46/bcbp/master/src/decode.spec.js
 *
 * Parser-structure inspiration:
 * - BoardingPassKit's section/cursor-style decoding approach.
 */
object IataBcbp {

    data class Parsed(
        val formatCode: String,
        val numberOfLegs: Int,
        val passengerName: String,
        val ticketIndicator: String,
        val versionNumberIndicator: String?,
        val versionNumber: Int?,
        val legs: List<Leg>,
        val uniqueConditional: UniqueConditional?,
        val securityData: SecurityData?,
        val airlineData: String?,
    ) {
        val firstLeg: Leg? get() = legs.firstOrNull()

        val fromAirport: String get() = firstLeg?.fromAirport.orEmpty()
        val toAirport: String get() = firstLeg?.toAirport.orEmpty()
        val carrierCode: String get() = firstLeg?.operatingCarrier.orEmpty()
        val flightNumber: String get() = firstLeg?.flightNumber.orEmpty()
        val flightDate: LocalDate? get() = firstLeg?.flightDate
        val travelClass: String get() = firstLeg?.compartmentCode.orEmpty()
        val seat: String get() = firstLeg?.seatNumber.orEmpty()
        val pnr: String get() = firstLeg?.pnrCode.orEmpty()
        val checkInSequence: String get() = firstLeg?.checkInSequenceNumber.orEmpty()
        val passengerStatus: String get() = firstLeg?.passengerStatus.orEmpty()

        fun flightCode(): String = firstLeg?.flightCode().orEmpty()

        fun summary(): String {
            val route = if (fromAirport.isNotBlank() && toAirport.isNotBlank()) "$fromAirport->$toAirport" else ""
            val flight = flightCode()
            val seatLabel = if (seat.isNotBlank()) "Seat $seat" else ""
            return listOf(route, flight, seatLabel).filter { it.isNotBlank() }.joinToString(" | ")
        }
    }

    data class Leg(
        val pnrCode: String,
        val fromAirport: String,
        val toAirport: String,
        val operatingCarrier: String,
        val flightNumber: String,
        val flightDate: LocalDate?,
        val compartmentCode: String,
        val seatNumber: String,
        val checkInSequenceNumber: String,
        val passengerStatus: String,
        val conditionalSize: Int,
        val repeatedConditional: RepeatedConditional?,
    ) {
        fun flightCode(): String {
            val normalizedFlight = flightNumber.trimStart('0')
            val number = normalizedFlight.ifBlank { flightNumber }
            return operatingCarrier + number
        }
    }

    data class UniqueConditional(
        val passengerDescription: String?,
        val checkInSource: String?,
        val boardingPassIssuanceSource: String?,
        val issuanceDate: LocalDate?,
        val documentType: String?,
        val issuingAirline: String?,
        val bagTagNumbers: List<String>,
    )

    data class RepeatedConditional(
        val airlineNumericCode: String?,
        val documentSerialNumber: String?,
        val selecteeIndicator: String?,
        val internationalDocumentVerification: String?,
        val marketingCarrierDesignator: String?,
        val frequentFlyerAirlineDesignator: String?,
        val frequentFlyerNumber: String?,
        val idAdIndicator: String?,
        val freeBaggageAllowance: String?,
        val fastTrack: Boolean?,
        val airlineUse: String?,
    )

    data class SecurityData(
        val type: String,
        val data: String,
    )

    private const val HEADER_LENGTH = 23
    private const val LEG_MANDATORY_LENGTH = 37

    private val supportedFormats = setOf(
        BarcodeFormat.AZTEC,
        BarcodeFormat.PDF_417,
        BarcodeFormat.QR_CODE,
        BarcodeFormat.DATA_MATRIX,
    )

    fun isBcbp(format: BarcodeFormat, rawMessage: String): Boolean {
        return parse(format, rawMessage) != null
    }

    fun parse(format: BarcodeFormat, rawMessage: String): Parsed? {
        if (format !in supportedFormats) return null

        val message = normalize(rawMessage)
        if (message.length < HEADER_LENGTH + LEG_MANDATORY_LENGTH) return null

        val cursor = Cursor(message)

        val formatCode = cursor.read(1) ?: return null
        if (formatCode != "M" && formatCode != "S") return null

        val numberOfLegs = cursor.read(1)?.toIntOrNull() ?: return null
        if (numberOfLegs !in 1..9) return null

        val passengerNameRaw = cursor.read(20) ?: return null
        if (!passengerNameRaw.isPassengerNameBlock()) return null
        val passengerName = passengerNameRaw.prettyPassengerName()

        val ticketIndicator = cursor.read(1)?.trim().orEmpty()

        val legs = mutableListOf<Leg>()
        var versionIndicator: String? = null
        var versionNumber: Int? = null
        var uniqueConditional: UniqueConditional? = null
        val referenceYear = LocalDate.now(ZoneOffset.UTC).year

        for (legIndex in 0 until numberOfLegs) {
            val mandatory = parseMandatoryLeg(cursor) ?: return null

            val conditionalPayload = cursor.read(mandatory.conditionalSize) ?: return null
            val conditionalCursor = Cursor(conditionalPayload)

            if (legIndex == 0 && mandatory.conditionalSize > 0) {
                versionIndicator = conditionalCursor.read(1)?.trimEnd()
                versionNumber = conditionalCursor.read(1)?.trim()?.toIntOrNull()
                val uniqueSize = conditionalCursor.readHex()
                if (uniqueSize != null) {
                    val uniquePayload = conditionalCursor.read(uniqueSize) ?: return null
                    uniqueConditional = parseUniqueConditional(uniquePayload, referenceYear)
                }
            }

            val repeatedConditional = if (conditionalCursor.remaining() >= 2) {
                parseRepeatedConditional(conditionalCursor)
            } else {
                null
            }

            val leg = mandatory.copy(
                repeatedConditional = repeatedConditional
            )
            legs += leg
        }

        val issuedDate = uniqueConditional?.issuanceDate
        val legsWithResolvedDates = legs.map { leg ->
            leg.copy(
                flightDate = resolveFlightDate(leg.flightDate, issuedDate, referenceYear)
            )
        }

        val securityData = parseSecurityData(cursor)
        val airlineData = cursor.readRemaining()?.trim()?.ifBlank { null }

        return Parsed(
            formatCode = formatCode,
            numberOfLegs = numberOfLegs,
            passengerName = passengerName,
            ticketIndicator = ticketIndicator,
            versionNumberIndicator = versionIndicator,
            versionNumber = versionNumber,
            legs = legsWithResolvedDates,
            uniqueConditional = uniqueConditional,
            securityData = securityData,
            airlineData = airlineData,
        )
    }

    private fun parseMandatoryLeg(cursor: Cursor): Leg? {
        val pnr = cursor.read(7)?.trim().orEmpty()
        val from = cursor.read(3)?.trim().orEmpty()
        val to = cursor.read(3)?.trim().orEmpty()
        val carrier = cursor.read(3)?.trim().orEmpty()
        val flight = cursor.read(5)?.trim()?.normalizePaddedNumberWithOptionalSuffix().orEmpty()
        val dayOfYear = cursor.read(3)?.trim()?.toIntOrNull()
        val compartment = cursor.read(1)?.trim().orEmpty()
        val seat = cursor.read(4)?.trim()?.normalizePaddedNumberWithOptionalSuffix().orEmpty()
        val checkIn = cursor.read(5)?.trim()?.normalizePaddedNumberWithOptionalSuffix().orEmpty()
        val passengerStatus = cursor.read(1)?.trim().orEmpty()
        val conditionalSize = cursor.readHex() ?: return null

        if (from.length != 3 || to.length != 3) return null
        if (carrier.length !in 2..3) return null
        if (flight.isBlank()) return null
        if (dayOfYear != null && dayOfYear !in 1..366) return null

        return Leg(
            pnrCode = pnr,
            fromAirport = from,
            toAirport = to,
            operatingCarrier = carrier,
            flightNumber = flight,
            flightDate = dayOfYear?.let { decodeDayOfYear(it, LocalDate.now(ZoneOffset.UTC).year) },
            compartmentCode = compartment,
            seatNumber = seat,
            checkInSequenceNumber = checkIn,
            passengerStatus = passengerStatus,
            conditionalSize = conditionalSize,
            repeatedConditional = null,
        )
    }

    private fun parseUniqueConditional(rawSection: String, referenceYear: Int): UniqueConditional {
        val cursor = Cursor(rawSection)
        val passengerDescription = cursor.read(1).cleanOptional()
        val checkInSource = cursor.read(1).cleanOptional()
        val boardingPassIssuanceSource = cursor.read(1).cleanOptional()
        val issuanceDate = decodeIssueDate(cursor.read(4), referenceYear)
        val documentType = cursor.read(1).cleanOptional()
        val issuingAirline = cursor.read(3).cleanOptional()
        val bagTags = mutableListOf<String>()

        while (cursor.remaining() >= 13) {
            cursor.read(13).cleanOptional()?.let { bagTags += it }
        }

        return UniqueConditional(
            passengerDescription = passengerDescription,
            checkInSource = checkInSource,
            boardingPassIssuanceSource = boardingPassIssuanceSource,
            issuanceDate = issuanceDate,
            documentType = documentType,
            issuingAirline = issuingAirline,
            bagTagNumbers = bagTags,
        )
    }

    private fun parseRepeatedConditional(cursor: Cursor): RepeatedConditional? {
        val sectionSize = cursor.readHex() ?: return null
        if (sectionSize <= 0 || cursor.remaining() < sectionSize) return null

        val section = Cursor(cursor.read(sectionSize) ?: return null)
        val airlineNumericCode = section.read(3).cleanOptional()
        val documentSerialNumber = section.read(10).cleanOptional()
        val selecteeIndicator = section.read(1).cleanOptional()
        val internationalDocumentVerification = section.read(1).cleanOptional()
        val marketingCarrierDesignator = section.read(3).cleanOptional()

        val remainingAfterFixed = section.remaining()
        val frequentFlyerSize = (remainingAfterFixed - 5).coerceAtLeast(0)
        val frequentFlyerRaw = section.read(frequentFlyerSize).orEmpty()
        val frequentFlyerAirlineDesignator = frequentFlyerRaw.take(3).cleanOptional()
        val frequentFlyerNumber = frequentFlyerRaw.drop(3).cleanOptional()

        val idAdIndicator = section.read(1).cleanOptional()
        val freeBaggageAllowance = section.read(3).cleanOptional()
        val fastTrack = section.read(1)?.trim()?.let {
            when (it) {
                "Y" -> true
                "N" -> false
                else -> null
            }
        }
        val airlineUse = section.readRemaining().cleanOptional()

        return RepeatedConditional(
            airlineNumericCode = airlineNumericCode,
            documentSerialNumber = documentSerialNumber,
            selecteeIndicator = selecteeIndicator,
            internationalDocumentVerification = internationalDocumentVerification,
            marketingCarrierDesignator = marketingCarrierDesignator,
            frequentFlyerAirlineDesignator = frequentFlyerAirlineDesignator,
            frequentFlyerNumber = frequentFlyerNumber,
            idAdIndicator = idAdIndicator,
            freeBaggageAllowance = freeBaggageAllowance,
            fastTrack = fastTrack,
            airlineUse = airlineUse,
        )
    }

    private fun parseSecurityData(cursor: Cursor): SecurityData? {
        if (cursor.remaining() < 4) return null
        if (cursor.peek() != "^") return null

        cursor.read(1) // marker
        val type = cursor.read(1)?.trim().orEmpty()
        val length = cursor.readHex() ?: return null
        val data = cursor.read(length)?.trimEnd() ?: return null
        return SecurityData(type = type, data = data)
    }

    private fun normalize(rawMessage: String): String {
        val withoutLineBreaks = rawMessage.replace("\r", "").replace("\n", "")
        return if (withoutLineBreaks.length > 3 && withoutLineBreaks[0] == ']') {
            withoutLineBreaks.drop(3).trimStart()
        } else {
            withoutLineBreaks.trimStart()
        }
    }

    private fun resolveFlightDate(
        candidate: LocalDate?,
        issuanceDate: LocalDate?,
        referenceYear: Int,
    ): LocalDate? {
        if (candidate == null) return null
        if (issuanceDate == null) return candidate
        val dateInIssueYear = decodeDayOfYear(candidate.dayOfYear, issuanceDate.year) ?: return candidate
        return if (dateInIssueYear.isBefore(issuanceDate)) dateInIssueYear.plusYears(1) else dateInIssueYear
    }

    private fun decodeDayOfYear(dayOfYear: Int, year: Int): LocalDate? {
        if (dayOfYear !in 1..366) return null
        return runCatching { LocalDate.ofYearDay(year, dayOfYear) }.getOrNull()
    }

    private fun decodeIssueDate(field: String?, referenceYear: Int): LocalDate? {
        val cleaned = field?.trim().orEmpty()
        if (cleaned.length != 4) return null

        val yearDigit = cleaned[0].digitToIntOrNull() ?: return null
        val dayOfYear = cleaned.substring(1).toIntOrNull() ?: return null
        if (dayOfYear !in 1..366) return null

        val year = closestYearWithLastDigit(referenceYear, yearDigit)
        return decodeDayOfYear(dayOfYear, year)
    }

    private fun closestYearWithLastDigit(referenceYear: Int, digit: Int): Int {
        val candidates = (referenceYear - 20..referenceYear + 20).filter { ((it % 10) + 10) % 10 == digit }
        return candidates.minByOrNull { abs(it - referenceYear) } ?: referenceYear
    }

    private fun String?.cleanOptional(): String? {
        return this?.trim()?.ifBlank { null }
    }

    private fun String.isPassengerNameBlock(): Boolean {
        return all { it.isUpperCase() || it.isDigit() || it == ' ' || it == '/' || it == '-' }
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

    private class Cursor(
        private val raw: String,
    ) {
        private var index: Int = 0

        fun remaining(): Int = raw.length - index

        fun read(length: Int): String? {
            if (length < 0 || index + length > raw.length) return null
            val value = raw.substring(index, index + length)
            index += length
            return value
        }

        fun readHex(): Int? = read(2)?.trim()?.ifBlank { "0" }?.let {
            runCatching { it.toInt(16) }.getOrNull()
        }

        fun readRemaining(): String? {
            if (index >= raw.length) return null
            val value = raw.substring(index)
            index = raw.length
            return value
        }

        fun peek(): String? = if (index < raw.length) raw.substring(index, index + 1) else null
    }
}
