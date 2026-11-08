package nz.eloque.foss_wallet.parsing

import nz.eloque.foss_wallet.model.PassBeacon
import nz.eloque.foss_wallet.model.PassImages
import nz.eloque.foss_wallet.model.field.DataDetectorType
import nz.eloque.foss_wallet.model.field.PassField
import nz.eloque.foss_wallet.model.field.TextAlignment
import nz.eloque.foss_wallet.persistence.TypeConverters
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PassDataTest {
    private val converters = TypeConverters()

    @Test
    fun `parses field alignment, data detectors and row`() {
        val field =
            FieldParser.parse(
                JSONObject()
                    .put("key", "k")
                    .put("value", "v")
                    .put("textAlignment", "PKTextAlignmentRight")
                    .put("dataDetectorTypes", JSONArray().put("PKDataDetectorTypeLink").put("PKDataDetectorTypeUnknown"))
                    .put("row", 1),
            )

        assertEquals(TextAlignment.RIGHT, field.textAlignment)
        assertEquals(setOf(DataDetectorType.LINK), field.dataDetectorTypes)
        assertEquals(1, field.row)
        assertEquals(field, PassField.fromJson(field.toJson()))
    }

    @Test
    fun `distinguishes default data detectors from disabled ones`() {
        val default = FieldParser.parse(JSONObject().put("key", "k").put("value", "v"))
        val disabled = FieldParser.parse(JSONObject().put("key", "k").put("value", "v").put("dataDetectorTypes", JSONArray()))

        assertNull(default.dataDetectorTypes)
        assertEquals(emptySet<DataDetectorType>(), disabled.dataDetectorTypes)
        assertEquals(default, PassField.fromJson(default.toJson()))
        assertEquals(disabled, PassField.fromJson(disabled.toJson()))
    }

    @Test
    fun `parses additionalInfoFields`() {
        val pass =
            parse {
                it.put(
                    "eventTicket",
                    JSONObject().put("additionalInfoFields", JSONArray().put(JSONObject().put("key", "info").put("value", "Doors at 7"))),
                )
            }

        assertEquals("info", pass.additionalInfoFields.single().key)
    }

    @Test
    fun `parses known links only`() {
        val pass = parse { it.put("transferURL", "https://example.com/transfer").put("unknownURL", "https://example.com") }

        assertEquals(mapOf("transferURL" to "https://example.com/transfer"), pass.links)
        assertEquals(pass.links, converters.toLinks(converters.fromLinks(pass.links)))
    }

    @Test
    fun `parses beacons and skips invalid ones`() {
        val pass =
            parse {
                it.put(
                    "beacons",
                    JSONArray()
                        .put(
                            JSONObject()
                                .put(
                                    "proximityUUID",
                                    "E2C56DB5-DFFB-48D2-B060-D0F5A71096E0",
                                ).put("major", 1)
                                .put("relevantText", "Store"),
                        ).put(JSONObject().put("major", 2)),
                )
            }

        assertEquals(listOf(PassBeacon("E2C56DB5-DFFB-48D2-B060-D0F5A71096E0", major = 1, relevantText = "Store")), pass.beacons)
        assertEquals(pass.beacons, converters.toBeacons(converters.fromBeacons(pass.beacons)))
    }

    @Test
    fun `new data is empty by default`() {
        val pass = parse { it }

        assertTrue(pass.links.isEmpty())
        assertTrue(pass.beacons.isEmpty())
        assertTrue(pass.additionalInfoFields.isEmpty())
    }

    private fun parse(configure: (JSONObject) -> JSONObject) =
        PassParser().parse(
            configure(
                JSONObject()
                    .put("formatVersion", 1)
                    .put("description", "Example ticket")
                    .put("organizationName", "Example venue")
                    .put("serialNumber", "pass-data-test"),
            ),
            images = PassImages(emptyMap()),
        )
}
