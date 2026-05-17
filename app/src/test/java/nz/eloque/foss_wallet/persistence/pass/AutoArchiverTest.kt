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

    private fun pass(expiration: Instant? = null): Pass =
        Pass(
            id = "id",
            description = "desc",
            formatVersion = 1,
            organization = "org",
            serialNumber = "serial",
            type = PassType.Generic,
            barCodes = emptySet(),
            addedAt = now,
            expirationDate = expiration?.atZone(ZoneOffset.UTC),
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
}
