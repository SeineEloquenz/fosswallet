package nz.eloque.foss_wallet.persistence.backup

import android.location.Location
import androidx.compose.ui.graphics.Color
import com.google.zxing.BarcodeFormat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import nz.eloque.foss_wallet.persistence.TypeConverters
import java.nio.charset.Charset
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.FormatStyle
import java.util.UUID

/**
 * Custom [KSerializer]s for the Android / `java.time` / library leaf types embedded in [Pass] and
 * friends, plus the configured [backupJson].
 *
 * Every leaf is written as a single string. ZonedDateTime, UUID and Color reuse the wire form the
 * Room [TypeConverters] already define so that choice lives in one place; the rest define their own.
 */

private val converters = TypeConverters()

/** Builds a [KSerializer] for a leaf type that is written as a single string. */
private fun <T> stringSerializer(
    name: String,
    encode: (T) -> String,
    decode: (String) -> T,
): KSerializer<T> =
    object : KSerializer<T> {
        override val descriptor = PrimitiveSerialDescriptor(name, PrimitiveKind.STRING)

        override fun serialize(
            encoder: Encoder,
            value: T,
        ) = encoder.encodeString(encode(value))

        override fun deserialize(decoder: Decoder): T = decode(decoder.decodeString())
    }

private val InstantSerializer = stringSerializer("Instant", Instant::toString, Instant::parse)
private val ZonedDateTimeSerializer = stringSerializer("ZonedDateTime", converters::fromZonedDateTime, converters::toZonedDateTime)
private val UuidSerializer = stringSerializer("UUID", converters::fromUuid, converters::toUuid)
private val ColorSerializer = stringSerializer<Color>("Color", converters::fromColor, converters::toColor)
private val CharsetSerializer = stringSerializer<Charset>("Charset", { it.name() }, Charset::forName)
private val BarcodeFormatSerializer = stringSerializer<BarcodeFormat>("BarcodeFormat", { it.name }, BarcodeFormat::valueOf)
private val FormatStyleSerializer = stringSerializer<FormatStyle>("FormatStyle", { it.name }, FormatStyle::valueOf)
private val LocationSerializer =
    stringSerializer<Location>(
        "Location",
        { "${it.latitude},${it.longitude}" },
        { encoded ->
            val (latitude, longitude) = encoded.split(",")
            Location("").apply {
                this.latitude = latitude.toDouble()
                this.longitude = longitude.toDouble()
            }
        },
    )

private val backupModule =
    SerializersModule {
        contextual(Instant::class, InstantSerializer)
        contextual(ZonedDateTime::class, ZonedDateTimeSerializer)
        contextual(UUID::class, UuidSerializer)
        contextual(Color::class, ColorSerializer)
        contextual(Charset::class, CharsetSerializer)
        contextual(Location::class, LocationSerializer)
        contextual(BarcodeFormat::class, BarcodeFormatSerializer)
        contextual(FormatStyle::class, FormatStyleSerializer)
    }

/** The single [Json] instance used to read/write `.fosswallet` payloads. */
internal val backupJson: Json =
    Json {
        serializersModule = backupModule
        encodeDefaults = true
        ignoreUnknownKeys = true
        classDiscriminator = "kind"
    }
