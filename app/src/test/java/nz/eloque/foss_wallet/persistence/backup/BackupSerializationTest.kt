package nz.eloque.foss_wallet.persistence.backup

import android.location.Location
import androidx.compose.ui.graphics.Color
import com.google.zxing.BarcodeFormat
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassColors
import nz.eloque.foss_wallet.model.PassLocalization
import nz.eloque.foss_wallet.model.PassMetadata
import nz.eloque.foss_wallet.model.PassRelevantDate
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.model.Tag
import nz.eloque.foss_wallet.model.TransitType
import nz.eloque.foss_wallet.model.field.PassContent
import nz.eloque.foss_wallet.model.field.PassField
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.FormatStyle
import java.util.UUID

class BackupSerializationTest {
    private val json = backupJson

    private fun roundTrip(pass: Pass): Pass = json.decodeFromString(json.encodeToString(pass))

    private fun samplePass(id: String) =
        Pass(
            id = id,
            description = "Sample $id",
            formatVersion = 1,
            organization = "Org",
            serialNumber = "SN-$id",
            type = PassType.Generic,
            barCodes = setOf(BarCode(BarcodeFormat.QR_CODE, "m-$id", Charsets.UTF_8, null)),
            addedAt = Instant.ofEpochMilli(0),
        )

    @Test
    fun roundTripsMinimalPass() {
        val pass =
            samplePass("abc123").copy(
                deviceId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
            )
        assertEquals(pass, roundTrip(pass))
    }

    @Test
    fun roundTripsFullyPopulatedPass() {
        val zoned = ZonedDateTime.parse("2024-01-15T10:30:00+01:00")
        val later = ZonedDateTime.parse("2024-06-30T12:00:00+02:00")
        val pass =
            Pass(
                id = "full-pass",
                description = "Boarding Pass",
                formatVersion = 1,
                organization = "Airline",
                serialNumber = "SN-99",
                type = PassType.Boarding(TransitType.AIR),
                barCodes =
                    setOf(
                        BarCode(BarcodeFormat.QR_CODE, "qr-msg", Charsets.UTF_8, "alt"),
                        BarCode(BarcodeFormat.CODE_128, "code-msg", Charsets.ISO_8859_1, null),
                    ),
                addedAt = Instant.ofEpochMilli(1_700_000_000_000),
                hasLogo = true,
                hasStrip = true,
                hasThumbnail = true,
                hasFooter = true,
                deviceId = UUID.fromString("11111111-2222-3333-4444-555555555555"),
                colors = PassColors(Color(0xFF112233), Color(0xFFAABBCC), Color(0xFF445566)),
                relevantDates =
                    listOf(
                        PassRelevantDate.Date(zoned),
                        PassRelevantDate.DateInterval(zoned, later),
                    ),
                expirationDate = ZonedDateTime.parse("2025-12-31T23:59:59Z"),
                logoText = "Logo",
                authToken = "tok",
                webServiceUrl = "https://example.org/pass",
                passTypeIdentifier = "pass.com.example",
                headerFields = listOf(PassField("h1", "Header", PassContent.Plain("HV"), changeMessage = "changed %@")),
                primaryFields = listOf(PassField("p1", "Primary", PassContent.Currency("12.50", "EUR"))),
                secondaryFields =
                    listOf(
                        PassField("s1", null, PassContent.Date(zoned, FormatStyle.MEDIUM, ignoresTimeZone = true, isRelative = false)),
                    ),
                auxiliaryFields =
                    listOf(
                        PassField("a1", "Aux", PassContent.Time(zoned, FormatStyle.SHORT, ignoresTimeZone = false, isRelative = true)),
                    ),
                backFields =
                    listOf(
                        PassField("b1", "Back", PassContent.DateTime(zoned, FormatStyle.LONG, ignoresTimeZone = false, isRelative = false)),
                    ),
            )
        assertEquals(pass, roundTrip(pass))
    }

    @Test
    fun preservesLocations() {
        val pass = samplePass("loc-pass").copy(locations = listOf(Location("gps"), Location("network")))
        // Location getters return 0.0 in JVM unit tests, so assert structural preservation only.
        assertEquals(2, roundTrip(pass).locations.size)
    }

    @Test
    fun entryRoundTrips() {
        val entry =
            BackupEntry(
                pass = samplePass("pid"),
                metadata = PassMetadata(passId = "pid", groupId = 5L, archived = true, autoArchive = false, renderLegacy = true),
                tagLabels = listOf("travel", "work"),
                localizations =
                    listOf(
                        PassLocalization("pid", "en", "Label", "Text"),
                        PassLocalization("pid", "de", "Etikett", "Inhalt"),
                    ),
                attachmentNames = listOf("ticket.pdf", "map.png"),
            )
        assertEquals(entry, json.decodeFromString<BackupEntry>(json.encodeToString(entry)))
    }

    @Test
    fun manifestRoundTrips() {
        val manifest =
            BackupManifest(
                formatVersion = BackupFormat.FORMAT_VERSION,
                createdAt = "2026-06-21T00:00:00Z",
                appVersionName = "1.2.3",
                appVersionCode = 42L,
                tags = listOf(Tag("travel", Color(0xFF00FF00)), Tag("work", Color(0xFF112233))),
                passIds = listOf("id1", "id2", "id3"),
            )
        assertEquals(manifest, json.decodeFromString<BackupManifest>(json.encodeToString(manifest)))
    }
}
