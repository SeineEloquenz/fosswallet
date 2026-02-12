package nz.eloque.foss_wallet.model

import android.location.Location
import nz.eloque.foss_wallet.model.field.PassContent
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
        logoText: String = "",
        colors: PassColors? = null,
        location: Location? = null,
        relevantDates: List<PassRelevantDate> = emptyList(),
        expirationDate: ZonedDateTime? = null,
    ): Pass? {
        try {
            barCode.encodeAsBitmap(100, 100, false)
        } catch (_: IllegalArgumentException) {
            return null
        }

        val id = Hash.sha256(barCode.toString())

        val nameField = PassField(
            key = "main",
            label = "",
            content = PassContent.Plain(name)
        )

        return Pass(
            id = id,
            description = name,
            formatVersion = FORMAT_VERSION,
            organization = organization.ifBlank { ORGANIZATION },
            serialNumber = serialNumber.ifBlank { id },
            type = type,
            barCodes = setOf(barCode),
            addedAt = Instant.now(),
            logoText = logoText.ifBlank { null },
            colors = colors,
            locations = location?.let { listOf(it) } ?: emptyList(),
            relevantDates = relevantDates,
            expirationDate = expirationDate,
            primaryFields = listOf(nameField),
        )
    }
}
