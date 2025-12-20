package nz.eloque.foss_wallet.model

import nz.eloque.foss_wallet.model.field.PassContent
import nz.eloque.foss_wallet.model.field.PassField
import nz.eloque.foss_wallet.utils.Hash
import java.time.Instant

object PassCreator {

    const val FORMAT_VERSION = 1
    const val ORGANIZATION = "nz.eloque.foss_wallet"

    fun create(name: String, type: PassType, barCode: BarCode): Pass? {
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
            organization = ORGANIZATION,
            serialNumber = id,
            type = type,
            barCodes = setOf(barCode),
            addedAt = Instant.now(),
            primaryFields = listOf(nameField)
        )
    }
}