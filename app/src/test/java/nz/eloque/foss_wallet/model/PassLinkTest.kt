package nz.eloque.foss_wallet.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PassLinkTest {
    @Test
    fun `opens http and https web links`() {
        assertEquals("https://example.com/manage", PassLink.MANAGEMENT.uri(" https://example.com/manage ").toString())
        assertEquals(
            "http://example.com",
            PassLink.TRANSFER
                .uri("HTTP://example.com")
                ?.toString()
                ?.lowercase(),
        )
    }

    @Test
    fun `rejects web links with other schemes`() {
        assertNull(PassLink.MANAGEMENT.uri("intent://example#Intent;end"))
        assertNull(PassLink.MANAGEMENT.uri("javascript:alert(1)"))
        assertNull(PassLink.MANAGEMENT.uri("example.com"))
    }

    @Test
    fun `builds phone and email uris`() {
        assertEquals("tel", PassLink.CONTACT_VENUE_PHONE_NUMBER.uri("+64 9 123 4567")?.scheme)
        assertEquals("+64 9 123 4567", PassLink.CONTACT_VENUE_PHONE_NUMBER.uri("+64 9 123 4567")?.schemeSpecificPart)
        assertEquals("mailto", PassLink.TRANSIT_PROVIDER_EMAIL.uri("help@example.com")?.scheme)
        assertEquals("help@example.com", PassLink.TRANSIT_PROVIDER_EMAIL.uri("help@example.com")?.schemeSpecificPart)
    }

    @Test
    fun `lists openable links in display order`() {
        val links =
            PassLink.of(
                mapOf(
                    "contactVenueEmail" to "venue@example.com",
                    "transferURL" to "https://example.com/transfer",
                    "managementURL" to "https://example.com/manage",
                    "sellURL" to "ftp://example.com",
                    "unknownURL" to "https://example.com",
                ),
            )

        assertEquals(listOf(PassLink.MANAGEMENT, PassLink.TRANSFER, PassLink.CONTACT_VENUE_EMAIL), links.map { it.first })
    }
}
