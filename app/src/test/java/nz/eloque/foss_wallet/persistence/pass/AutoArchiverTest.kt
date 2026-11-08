package nz.eloque.foss_wallet.persistence.pass

import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassMetadata
import nz.eloque.foss_wallet.model.PassType
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

class AutoArchiverTest {
    private val now: Instant = Instant.parse("2024-01-01T12:00:00Z")

    private fun pass(
        expiration: Instant? = null,
        added: Instant = now.minusSeconds(3600),
        semantics: String? = null,
    ): Pass =
        Pass(
            id = "id",
            description = "desc",
            formatVersion = 1,
            organization = "org",
            serialNumber = "serial",
            type = PassType.Generic,
            barCodes = emptySet(),
            addedAt = added,
            expirationDate = expiration?.atZone(ZoneOffset.UTC),
            semantics = semantics,
        )

    private fun metadata(
        archived: Boolean = false,
        autoArchive: Boolean = true,
    ) = PassMetadata(
        passId = "id",
        archived = archived,
        autoArchive = autoArchive,
    )

    @Test
    fun `returns true when already archived`() {
        val result =
            AutoArchiver.shouldBeAutoArchived(
                pass = pass(expiration = now.plusSeconds(999)),
                metadata = metadata(archived = true),
                now = now,
            )

        assertTrue(result)
    }

    @Test
    fun `returns true when archived even without expiration`() {
        val result =
            AutoArchiver.shouldBeAutoArchived(
                pass = pass(expiration = null),
                metadata = metadata(archived = true),
                now = now,
            )

        assertTrue(result)
    }

    @Test
    fun `returns false when autoArchive disabled even if expired`() {
        val expired = now.minusSeconds(1)

        val result =
            AutoArchiver.shouldBeAutoArchived(
                pass = pass(expiration = expired),
                metadata = metadata(autoArchive = false),
                now = now,
            )

        assertFalse(result)
    }

    @Test
    fun `returns true when autoArchive enabled and expired`() {
        val expired = now.minusSeconds(60)

        val result =
            AutoArchiver.shouldBeAutoArchived(
                pass = pass(expiration = expired),
                metadata = metadata(autoArchive = true),
                now = now,
            )

        assertTrue(result)
    }

    @Test
    fun `returns false when expiration is in the future`() {
        val future = now.plusSeconds(60)

        val result =
            AutoArchiver.shouldBeAutoArchived(
                pass = pass(expiration = future),
                metadata = metadata(autoArchive = true),
                now = now,
            )

        assertFalse(result)
    }

    @Test
    fun `returns false when expiration is null`() {
        val result =
            AutoArchiver.shouldBeAutoArchived(
                pass = pass(expiration = null),
                metadata = metadata(autoArchive = true),
                now = now,
            )

        assertFalse(result)
    }

    @Test
    fun `expiration exactly at now counts as expired`() {
        val result =
            AutoArchiver.shouldBeAutoArchived(
                pass = pass(expiration = now),
                metadata = metadata(autoArchive = true),
                now = now,
            )

        assertTrue(result)
    }

    @Test
    fun `returns false when already expired at time of adding`() {
        val expired = now.minusSeconds(60)

        val result =
            AutoArchiver.shouldBeAutoArchived(
                pass = pass(expiration = expired, added = now),
                metadata = metadata(autoArchive = true),
                now = now,
            )

        assertFalse(result)
    }

    @Test
    fun `returns false when added exactly at expiration`() {
        val expired = now.minusSeconds(60)

        val result =
            AutoArchiver.shouldBeAutoArchived(
                pass = pass(expiration = expired, added = expired),
                metadata = metadata(autoArchive = true),
                now = now,
            )

        assertFalse(result)
    }

    @Test
    fun `returns true when a held pass becomes voided`() {
        val result =
            AutoArchiver.shouldBeAutoArchived(
                pass = pass().copy(voided = true),
                metadata = metadata(),
                previous = pass(),
                now = now,
            )

        assertTrue(result)
    }

    @Test
    fun `returns false when a pass is imported already voided`() {
        val result =
            AutoArchiver.shouldBeAutoArchived(
                pass = pass().copy(voided = true),
                metadata = metadata(),
                previous = null,
                now = now,
            )

        assertFalse(result)
    }

    @Test
    fun `returns false when a voided pass stays voided after being unarchived`() {
        val result =
            AutoArchiver.shouldBeAutoArchived(
                pass = pass().copy(voided = true),
                metadata = metadata(),
                previous = pass().copy(voided = true),
                now = now,
            )

        assertFalse(result)
    }

    @Test
    fun `returns false when a voided pass has autoArchive disabled`() {
        val result =
            AutoArchiver.shouldBeAutoArchived(
                pass = pass().copy(voided = true),
                metadata = metadata(autoArchive = false),
                previous = pass(),
                now = now,
            )

        assertFalse(result)
    }

    @Test
    fun `expiration comparison is timezone safe`() {
        val expiration =
            ZonedDateTime.of(
                2024,
                1,
                1,
                13,
                0,
                0,
                0,
                ZoneOffset.ofHours(1), // equals 12:00 UTC
            )

        val result =
            AutoArchiver.shouldBeAutoArchived(
                pass = pass(expiration = expiration.toInstant()),
                metadata = metadata(),
                now = now,
            )

        assertTrue(result)
    }

    private fun archivedWith(
        semantics: String,
        expiration: Instant? = null,
    ) = AutoArchiver.shouldBeAutoArchived(
        pass(expiration = expiration, added = now.minusSeconds(86400), semantics = semantics),
        metadata(),
        now = now,
    )

    @Test
    fun `archives after the event ended`() {
        assertTrue(archivedWith("""{"eventEndDate":"2024-01-01T11:00:00Z"}"""))
        assertFalse(archivedWith("""{"eventEndDate":"2024-01-01T13:00:00Z"}"""))
    }

    @Test
    fun `archives when the event ended before the expiration`() {
        assertTrue(archivedWith("""{"eventEndDate":"2024-01-01T11:00:00Z"}""", expiration = now.plusSeconds(86400)))
    }

    @Test
    fun `archives when the expiration is before the event end`() {
        assertTrue(archivedWith("""{"eventEndDate":"2024-01-02T00:00:00Z"}""", expiration = now.minusSeconds(60)))
    }

    @Test
    fun `uses start plus duration`() {
        assertTrue(archivedWith("""{"eventStartDate":"2024-01-01T10:00:00Z","duration":3600}"""))
        assertFalse(archivedWith("""{"eventStartDate":"2024-01-01T11:30:00Z","duration":3600}"""))
        assertTrue(archivedWith("""{"currentDepartureDate":"2024-01-01T10:00:00Z","duration":3600}"""))
    }

    @Test
    fun `prefers the current arrival and computed end over the original arrival`() {
        assertFalse(archivedWith("""{"originalArrivalDate":"2024-01-01T11:00:00Z","currentArrivalDate":"2024-01-01T13:00:00Z"}"""))
        assertFalse(
            archivedWith(
                """{"currentDepartureDate":"2024-01-01T11:30:00Z","duration":3600,"originalArrivalDate":"2024-01-01T11:00:00Z"}""",
            ),
        )
        assertTrue(archivedWith("""{"originalArrivalDate":"2024-01-01T11:00:00Z"}"""))
    }

    @Test
    fun `does not archive without a known end`() {
        assertFalse(archivedWith("""{"eventStartDate":"2024-01-01T10:00:00Z","venueName":"Hall"}"""))
        assertFalse(archivedWith("not json"))
    }
}
