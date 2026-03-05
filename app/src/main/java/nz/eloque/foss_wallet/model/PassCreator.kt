package nz.eloque.foss_wallet.model

import android.location.Location
import de.nielstron.bcbp.IataBcbp
import nz.eloque.foss_wallet.model.field.PassContent
import nz.eloque.foss_wallet.model.field.PassField
import nz.eloque.foss_wallet.utils.Hash
import nz.eloque.foss_wallet.utils.linkifyUrls
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.FormatStyle
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
        val nameField = PassField(
            key = "main",
            label = "",
            content = PassContent.Plain(name)
        )

        return createPass(
            name = name,
            type = type,
            barCodes = barCodes,
            organization = organization,
            serialNumber = serialNumber,
            logoText = logoText,
            colors = colors,
            location = location,
            relevantDates = relevantDates,
            expirationDate = expirationDate,
            primaryFields = listOf(nameField),
            secondaryFields = emptyList(),
            auxiliaryFields = emptyList(),
        )
    }

    fun createFromBCBP(
        bcbp: IataBcbp.Parsed,
        barCodes: List<BarCode>,
        name: String = bcbp.summary(),
        organization: String = ORGANIZATION,
        serialNumber: String = "",
        logoText: String = "",
        colors: PassColors? = null,
        location: Location? = null,
        relevantDates: List<PassRelevantDate> = emptyList(),
        expirationDate: ZonedDateTime? = null,
    ): Pass? {
        val primaryFields = listOfNotNull(
            plainField("from", "From", bcbp.fromAirport),
            plainField("to", "To", bcbp.toAirport),
        )
        val secondaryFields = listOfNotNull(
            plainField("flight", "Flight", bcbp.flightCode()),
            bcbp.flightDate?.let { flightDate ->
                PassField(
                    key = "date",
                    label = "Date",
                    content = PassContent.Date(
                        date = flightDate.atStartOfDay(ZoneId.systemDefault()),
                        format = FormatStyle.MEDIUM,
                        ignoresTimeZone = true,
                        isRelative = false,
                    ),
                )
            },
            plainField("class", "Class", bcbp.travelClass),
        )
        val auxiliaryFields = listOfNotNull(
            plainField("passenger", "Passenger", bcbp.passengerName),
            plainField("seat", "Seat", bcbp.seat),
        )

        return createPass(
            name = name,
            type = PassType.Boarding(TransitType.AIR),
            barCodes = barCodes,
            organization = organization,
            serialNumber = serialNumber,
            logoText = logoText,
            colors = colors,
            location = location,
            relevantDates = relevantDates,
            expirationDate = expirationDate,
            primaryFields = primaryFields,
            secondaryFields = secondaryFields,
            auxiliaryFields = auxiliaryFields,
        )
    }

    private fun createPass(
        name: String,
        type: PassType,
        barCodes: List<BarCode>,
        organization: String,
        serialNumber: String,
        logoText: String,
        colors: PassColors?,
        location: Location?,
        relevantDates: List<PassRelevantDate>,
        expirationDate: ZonedDateTime?,
        primaryFields: List<PassField>,
        secondaryFields: List<PassField>,
        auxiliaryFields: List<PassField>,
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
            content = PassContent.Plain(linkifyUrls(value)),
        )
    }
}
