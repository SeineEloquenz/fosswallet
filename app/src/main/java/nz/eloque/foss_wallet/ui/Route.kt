package nz.eloque.foss_wallet.ui

import com.google.zxing.BarcodeFormat
import kotlinx.serialization.Serializable
import nz.eloque.foss_wallet.model.BarCode
import java.nio.charset.Charset

/** Every navigation destination of the app. */
@Serializable
sealed interface Route {
    @Serializable
    data object Wallet : Route

    @Serializable
    data object Scan : Route

    @Serializable
    data object Archive : Route

    @Serializable
    data object About : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object Libraries : Route

    @Serializable
    data object AdvancedAdd : Route

    @Serializable
    data object Create : Route

    @Serializable
    data class CreateWithBarcode(
        val format: String,
        val message: String,
        val encoding: String,
        val altText: String? = null,
    ) : Route {
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

    @Serializable
    data class Pass(
        val passId: String,
    ) : Route

    @Serializable
    data class Webview(
        val url: String,
    ) : Route

    @Serializable
    data class UpdateFailure(
        val reason: String,
        val rationale: String,
    ) : Route
}
