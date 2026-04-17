package nz.eloque.foss_wallet.model

import android.location.Location
import nz.eloque.foss_wallet.model.field.PassField
import nz.eloque.foss_wallet.utils.Hash
import java.time.Instant
import java.time.ZonedDateTime

object PassCreator {
    const val FORMAT_VERSION = 1
    const val ORGANIZATION = "nz.eloque.foss_wallet"

    fun create(
        name: String,
        type: PassType,
        barCode: BarCode,
        organization: String = ORGANIZATION,
        serialNumber: String = "",
        colors: PassColors? = null,
        location: Location? = null,
        relevantDates: List<PassRelevantDate> = emptyList(),
        expirationDate: ZonedDateTime? = null,
        headerFields: List<PassField> = emptyList(),
        primaryFields: List<PassField> = emptyList(),
        secondaryFields: List<PassField> = emptyList(),
        auxiliaryFields: List<PassField> = emptyList(),
        backFields: List<PassField> = emptyList(),
    ): Pass? =
        create(
            name = name,
            type = type,
            barCodes = listOf(barCode),
            organization = organization,
            serialNumber = serialNumber,
            colors = colors,
            location = location,
            relevantDates = relevantDates,
            expirationDate = expirationDate,
            headerFields = headerFields,
            primaryFields = primaryFields,
            secondaryFields = secondaryFields,
            auxiliaryFields = auxiliaryFields,
            backFields = backFields,
        )

    fun create(
        name: String,
        type: PassType,
        barCodes: List<BarCode>,
        organization: String = ORGANIZATION,
        serialNumber: String = "",
        colors: PassColors? = null,
        location: Location? = null,
        relevantDates: List<PassRelevantDate> = emptyList(),
        expirationDate: ZonedDateTime? = null,
        headerFields: List<PassField> = emptyList(),
        primaryFields: List<PassField> = emptyList(),
        secondaryFields: List<PassField> = emptyList(),
        auxiliaryFields: List<PassField> = emptyList(),
        backFields: List<PassField> = emptyList(),
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
            logoText = name,
            colors = colors,
            locations = location?.let { listOf(it) } ?: emptyList(),
            relevantDates = relevantDates,
            expirationDate = expirationDate,
            headerFields = headerFields,
            primaryFields = primaryFields,
            secondaryFields = secondaryFields,
            auxiliaryFields = auxiliaryFields,
            backFields = backFields,
        )
    }
}
