package nz.eloque.foss_wallet.model

import nz.eloque.foss_wallet.utils.Hash
import java.time.Instant

class PassCreator {

    fun create(name: String, barCode: BarCode): Pass {
        val id = Hash.sha256(barCode.toString())
        return Pass(
            id = id,
            description = name,
            formatVersion = FORMAT_VERSION,
            organization = ORGANIZATION,
            serialNumber = id,
            type = PassType.Generic,
            barCodes = setOf(barCode),
            addedAt = Instant.now()
        )
    }

    companion object {
        const val FORMAT_VERSION = 1
        const val ORGANIZATION = "nz.eloque.foss_wallet"
    }
}