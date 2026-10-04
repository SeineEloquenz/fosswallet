package nz.eloque.foss_wallet.ui

import com.google.zxing.BarcodeFormat
import kotlinx.serialization.Serializable
import nz.eloque.foss_wallet.model.BarCode
import java.nio.charset.Charset

@Serializable
data class CreateWithBarcode(
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

    fun toBarCode(): BarCode =
        BarCode(
            format = BarcodeFormat.valueOf(format),
            message = message,
            encoding = Charset.forName(encoding),
            altText = altText,
        )
}
