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
    ): Pass =
        Pass(
            id = "pass-1",
            description = description,
            formatVersion = 1,
            organization = "org",
            serialNumber = "serial",
            type = PassType.Generic,
            barCodes = barCodes,
            addedAt = Instant.ofEpochMilli(0),
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
}
