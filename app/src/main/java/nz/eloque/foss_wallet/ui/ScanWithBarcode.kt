package nz.eloque.foss_wallet.ui

import kotlinx.serialization.Serializable
import nz.eloque.foss_wallet.model.BarCode

@Serializable
data class ScanWithBarcode(
    val format: String,
    val message: String,
    val encoding: String,
    val altText: String? = null,
) {
    constructor(barCode: BarCode) : this(
        format = barCode.format.name,
        message = barCode.message,
        encoding = barCode.encoding.name(),
        altText = barCode.altText,
    )

    fun toBarCode(): BarCode = CreateWithBarcode(format, message, encoding, altText).toBarCode()
}
