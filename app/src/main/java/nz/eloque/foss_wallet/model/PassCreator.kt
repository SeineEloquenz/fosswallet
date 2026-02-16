package nz.eloque.foss_wallet.model

import android.location.Location
import nz.eloque.foss_wallet.model.field.PassContent
import nz.eloque.foss_wallet.model.field.PassField
import nz.eloque.foss_wallet.utils.Hash
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.LinkedHashSet

object PassCreator {

    const val FORMAT_VERSION = 1
    const val ORGANIZATION = "nz.eloque.foss_wallet"

    fun create(
        name: String,
        type: PassType,
        barCode: BarCode,
        organization: String = ORGANIZATION,
        serialNumber: String = "",
        logoText: String = "",
        colors: PassColors? = null,
        location: Location? = null,
        relevantDates: List<PassRelevantDate> = emptyList(),
        expirationDate: ZonedDateTime? = null,
    ): Pass? {
        return create(
            name = name,
            type = type,
            barCodes = listOf(barCode),
            organization = organization,
            serialNumber = serialNumber,
            logoText = logoText,
            colors = colors,
            location = location,
            relevantDates = relevantDates,
            expirationDate = expirationDate,
        )
    }

    fun create(
        name: String,
        type: PassType,
        barCodes: List<BarCode>,
        organization: String = ORGANIZATION,
        serialNumber: String = "",
        logoText: String = "",
        colors: PassColors? = null,
        location: Location? = null,
        relevantDates: List<PassRelevantDate> = emptyList(),
        expirationDate: ZonedDateTime? = null,
    ): Pass? {
        if (barCodes.isEmpty()) {
            return null
        }

        if (barCodes.any {
                try {
                    it.encodeAsBitmap(100, 100, false)
                    false
                } catch (_: IllegalArgumentException) {
                    true
                }
            }
        ) {
            return null
        }

        val id = Hash.sha256(barCodes.joinToString("|") { it.toString() })

        val nameField = PassField(
            key = "main",
            label = "",
            content = PassContent.Plain(name)
        )

        val bcbp = if (type is PassType.Boarding && type.transitType == TransitType.AIR) {
            barCodes.firstNotNullOfOrNull { IataBcbp.parse(it.barcodeFormat(), it.rawMessage()) }
        } else {
            null
        }

        val parsedBcbpFields = bcbp?.let {
            Triple(
                listOfNotNull(
                    plainField("from", "From", it.fromAirport),
                    plainField("to", "To", it.toAirport),
                ),
                listOfNotNull(
                    plainField("flight", "Flight", it.flightCode()),
                    plainField("date", "Date", it.flightDate?.format(DateTimeFormatter.ISO_LOCAL_DATE).orEmpty()),
                    plainField("class", "Class", it.travelClass),
                ),
                listOfNotNull(
                    plainField("passenger", "Passenger", it.passengerName),
                    plainField("seat", "Seat", it.seat),
                ),
            )
        }

        val primaryFields = parsedBcbpFields?.first.orEmpty().ifEmpty { listOf(nameField) }
        val secondaryFields = parsedBcbpFields?.second.orEmpty()
        val auxiliaryFields = parsedBcbpFields?.third.orEmpty()

        return Pass(
            id = id,
            description = name,
            formatVersion = FORMAT_VERSION,
            organization = organization.ifBlank { ORGANIZATION },
            serialNumber = serialNumber.ifBlank { id },
            type = type,
            barCodes = LinkedHashSet(barCodes),
            addedAt = Instant.now(),
            logoText = logoText.ifBlank { null },
            colors = colors,
            locations = location?.let { listOf(it) } ?: emptyList(),
            relevantDates = relevantDates,
            expirationDate = expirationDate,
            primaryFields = primaryFields,
            secondaryFields = secondaryFields,
            auxiliaryFields = auxiliaryFields,
        )
    }

    private fun plainField(key: String, label: String, value: String): PassField? {
        if (value.isBlank()) return null
        return PassField(
            key = key,
            label = label,
            content = PassContent.Plain(value),
        )
    }
}
