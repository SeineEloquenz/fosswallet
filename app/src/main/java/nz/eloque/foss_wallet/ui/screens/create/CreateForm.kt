package nz.eloque.foss_wallet.ui.screens.create

import android.location.Location
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.serialization.saved
import androidx.navigation.toRoute
import com.google.zxing.BarcodeFormat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.model.PassColors
import nz.eloque.foss_wallet.model.PassRelevantDate
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.persistence.TypeConverters
import nz.eloque.foss_wallet.ui.Route
import java.nio.charset.Charset
import java.time.ZonedDateTime

/** Everything the user entered on the create screen. */
@Serializable
data class CreateForm(
    val name: String = "",
    val nameTouched: Boolean = false,
    val organization: String = "",
    val serialNumber: String = "",
    val barcodes: List<BarcodeDraft> = emptyList(),
    val activeBarcodeIndex: Int = 0,
    @Serializable(PassTypeSerializer::class) val type: PassType = PassType.Generic,
    @Serializable(LocationSerializer::class) val location: Location? = null,
    @Serializable(ZonedDateTimeSerializer::class) val relevantStart: ZonedDateTime? = null,
    @Serializable(ZonedDateTimeSerializer::class) val relevantEnd: ZonedDateTime? = null,
    @Serializable(ZonedDateTimeSerializer::class) val expirationDate: ZonedDateTime? = null,
    @Serializable(ColorSerializer::class) val backgroundColor: Color? = null,
    @Serializable(ColorSerializer::class) val foregroundColor: Color? = null,
    @Serializable(ColorSerializer::class) val labelColor: Color? = null,
    @Serializable(UriSerializer::class) val iconUrl: Uri? = null,
    @Serializable(UriSerializer::class) val logoUrl: Uri? = null,
    @Serializable(UriSerializer::class) val stripUrl: Uri? = null,
    @Serializable(UriSerializer::class) val thumbnailUrl: Uri? = null,
    @Serializable(UriSerializer::class) val footerUrl: Uri? = null,
    @Serializable(UriSerializer::class) val backgroundUrl: Uri? = null,
) {
    fun relevantDates(): List<PassRelevantDate> =
        when {
            relevantStart != null && relevantEnd != null -> listOf(PassRelevantDate.DateInterval(relevantStart, relevantEnd))
            relevantStart != null -> listOf(PassRelevantDate.Date(relevantStart))
            else -> emptyList()
        }

    fun passColors(): PassColors? {
        val fallback = backgroundColor ?: foregroundColor ?: labelColor ?: return null
        return PassColors(
            background = backgroundColor ?: fallback,
            foreground = foregroundColor ?: fallback,
            label = labelColor ?: fallback,
        )
    }
}

/** The create screen's form, kept in [savedStateHandle] so it survives configuration changes and process death. */
class CreateFormState(
    savedStateHandle: SavedStateHandle,
) {
    private var saved by savedStateHandle.saved {
        val initialBarcode = savedStateHandle.toRoute<Route.Create>().toBarCode()
        CreateForm(barcodes = listOfNotNull(initialBarcode?.let { BarcodeDraft.from(it) }))
    }
    private val _form = MutableStateFlow(saved)
    val form: StateFlow<CreateForm> = _form.asStateFlow()

    fun update(transform: (CreateForm) -> CreateForm) {
        _form.update(transform)
        saved = _form.value
    }
}

@Serializable
data class BarcodeDraft(
    val message: String,
    val altText: String,
    @Serializable(BarcodeFormatSerializer::class) val format: BarcodeFormat,
    @Serializable(CharsetSerializer::class) val encoding: Charset = Charsets.UTF_8,
) {
    fun toBarCode(): BarCode =
        BarCode(
            format = format,
            message = message,
            encoding = encoding,
            altText = altText.ifBlank { message.takeIf { encoding == Charsets.UTF_8 } },
        )

    companion object {
        fun from(barCode: BarCode) = BarcodeDraft(barCode.message, barCode.altText.orEmpty(), barCode.format, barCode.encoding)
    }
}

private val converters = TypeConverters()

private abstract class StringSerializer<T>(
    name: String,
) : KSerializer<T> {
    override val descriptor = PrimitiveSerialDescriptor("nz.eloque.foss_wallet.$name", PrimitiveKind.STRING)

    abstract fun toString(value: T): String

    abstract fun fromString(value: String): T

    override fun serialize(
        encoder: Encoder,
        value: T,
    ) = encoder.encodeString(toString(value))

    override fun deserialize(decoder: Decoder): T = fromString(decoder.decodeString())
}

private object UriSerializer : StringSerializer<Uri>("Uri") {
    override fun toString(value: Uri) = value.toString()

    override fun fromString(value: String) = value.toUri()
}

private object ZonedDateTimeSerializer : StringSerializer<ZonedDateTime>("ZonedDateTime") {
    override fun toString(value: ZonedDateTime) = converters.fromZonedDateTime(value)

    override fun fromString(value: String) = converters.toZonedDateTime(value)
}

private object ColorSerializer : StringSerializer<Color>("Color") {
    override fun toString(value: Color) = converters.fromColor(value)

    override fun fromString(value: String) = converters.toColor(value)
}

private object LocationSerializer : StringSerializer<Location>("Location") {
    override fun toString(value: Location) = "${value.latitude},${value.longitude}"

    override fun fromString(value: String): Location {
        val (latitude, longitude) = value.split(",").map { it.toDouble() }
        return Location("").also {
            it.latitude = latitude
            it.longitude = longitude
        }
    }
}

private object PassTypeSerializer : StringSerializer<PassType>("PassType") {
    override fun toString(value: PassType) = converters.fromPassType(value)

    override fun fromString(value: String) = converters.toPassType(value)
}

private object BarcodeFormatSerializer : StringSerializer<BarcodeFormat>("BarcodeFormat") {
    override fun toString(value: BarcodeFormat) = value.name

    override fun fromString(value: String) = BarcodeFormat.valueOf(value)
}

private object CharsetSerializer : StringSerializer<Charset>("Charset") {
    override fun toString(value: Charset): String = value.name()

    override fun fromString(value: String): Charset = Charset.forName(value)
}
