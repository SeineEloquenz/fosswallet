package nz.eloque.foss_wallet.model

import com.google.zxing.BarcodeFormat
import nz.eloque.foss_wallet.model.field.PassContent
import nz.eloque.foss_wallet.model.field.PassField
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class LocalizationTest {
    private fun barCode(altText: String?): BarCode = BarCode(BarcodeFormat.QR_CODE, "message", BarCode.FALLBACK_CHARSET, altText)

    private fun pass(
        barCodes: Set<BarCode> = setOf(),
        description: String = "desc",
        primaryFields: List<PassField> = listOf(),
        organization: String = "org",
        logoText: String? = null,
    ): Pass =
        Pass(
            id = "pass-1",
            description = description,
            formatVersion = 1,
            organization = organization,
            serialNumber = "serial",
            type = PassType.Generic,
            barCodes = barCodes,
            addedAt = Instant.ofEpochMilli(0),
            logoText = logoText,
            primaryFields = primaryFields,
        )

    @Test
    fun `localizes barcode altText via mapping`() {
        val localized =
            PassWithLocalization(
                pass = pass(barCodes = setOf(barCode("labelPnr"))),
                localizations = listOf(PassLocalization("pass-1", "en", "labelPnr", "TICKET: 12345")),
            ).applyLocalization("en")

        assertEquals("TICKET: 12345", localized.barCodes.single().altText)
    }

    @Test
    fun `leaves unmapped altText untouched`() {
        val localized =
            PassWithLocalization(
                pass = pass(barCodes = setOf(barCode("Boarding Pass"))),
                localizations = listOf(PassLocalization("pass-1", "en", "labelPnr", "TICKET: 12345")),
            ).applyLocalization("en")

        assertEquals("Boarding Pass", localized.barCodes.single().altText)
    }

    @Test
    fun `leaves null altText null`() {
        val localized =
            PassWithLocalization(
                pass = pass(barCodes = setOf(barCode(null))),
                localizations = listOf(PassLocalization("pass-1", "en", "labelPnr", "TICKET: 12345")),
            ).applyLocalization("en")

        assertNull(localized.barCodes.single().altText)
    }

    @Test
    fun `still localizes fields and description alongside barcodes`() {
        val localized =
            pass(
                barCodes = setOf(barCode("labelPnr")),
                description = "descKey",
                primaryFields = listOf(PassField("k", "labelKey", PassContent.Plain("valueKey"))),
            ).applyLocalization(
                mapOf(
                    "labelPnr" to PassLocalization("pass-1", "en", "labelPnr", "TICKET: 12345"),
                    "descKey" to PassLocalization("pass-1", "en", "descKey", "A ticket"),
                    "labelKey" to PassLocalization("pass-1", "en", "labelKey", "Passenger"),
                    "valueKey" to PassLocalization("pass-1", "en", "valueKey", "Jane Doe"),
                ),
            )

        assertEquals("A ticket", localized.description)
        assertEquals("Passenger", localized.primaryFields.single().label)
        assertEquals(
            "Jane Doe",
            localized.primaryFields
                .single()
                .content
                .prettyPrint(),
        )
        assertEquals("TICKET: 12345", localized.barCodes.single().altText)
    }

    @Test
    fun `localizes organizationName and logoText`() {
        val localized =
            pass(organization = "orgKey", logoText = "logoKey").applyLocalization(
                mapOf(
                    "orgKey" to PassLocalization("pass-1", "en", "orgKey", "Railways"),
                    "logoKey" to PassLocalization("pass-1", "en", "logoKey", "Rail"),
                ),
            )

        assertEquals("Railways", localized.organization)
        assertEquals("Rail", localized.logoText)
    }

    @Test
    fun `localizes additionalInfoFields`() {
        val localized =
            pass()
                .copy(additionalInfoFields = listOf(PassField("k", "labelKey", PassContent.Plain("valueKey"), "changeKey")))
                .applyLocalization(
                    mapOf(
                        "labelKey" to PassLocalization("pass-1", "en", "labelKey", "Lounge"),
                        "valueKey" to PassLocalization("pass-1", "en", "valueKey", "Gate B"),
                        "changeKey" to PassLocalization("pass-1", "en", "changeKey", "Lounge moved to %@"),
                    ),
                ).additionalInfoFields
                .single()

        assertEquals("Lounge", localized.label)
        assertEquals("Gate B", localized.content.prettyPrint())
        assertEquals("Lounge moved to Gate B", localized.changeMessage)
    }

    @Test
    fun `reports changed additionalInfoFields`() {
        val old = pass().copy(additionalInfoFields = listOf(PassField("k", "Lounge", PassContent.Plain("Gate A"), "Moved to %@")))
        val new = old.copy(additionalInfoFields = listOf(PassField("k", "Lounge", PassContent.Plain("Gate B"), "Moved to %@")))

        assertEquals("k", new.updatedFields(old).single().key)
    }

    @Test
    fun `leaves unmapped organizationName and absent logoText untouched`() {
        val localized = pass(organization = "Railways").applyLocalization(mapOf())

        assertEquals("Railways", localized.organization)
        assertNull(localized.logoText)
    }
}
