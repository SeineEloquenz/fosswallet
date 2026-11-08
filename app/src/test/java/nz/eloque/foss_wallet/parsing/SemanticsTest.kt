package nz.eloque.foss_wallet.parsing

import nz.eloque.foss_wallet.model.PassImages
import nz.eloque.foss_wallet.model.field.PassField
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SemanticsTest {
    private val passSemantics = JSONObject().put("eventName", "Concert").put("venueLocation", JSONObject().put("latitude", 52.52))
    private val fieldSemantics = JSONObject().put("seats", JSONArray().put(JSONObject().put("seatNumber", "12")))

    @Test
    fun `stores pass and field semantics as raw json`() {
        val pass =
            parse {
                it.put("semantics", passSemantics).put(
                    "eventTicket",
                    JSONObject().put(
                        "primaryFields",
                        JSONArray().put(JSONObject().put("key", "seat").put("value", "12").put("semantics", fieldSemantics)),
                    ),
                )
            }

        assertEquals(passSemantics.toString(), pass.semantics)
        assertEquals(fieldSemantics.toString(), pass.primaryFields.single().semantics)
    }

    @Test
    fun `has no semantics by default`() {
        val pass =
            parse {
                it.put(
                    "eventTicket",
                    JSONObject().put("primaryFields", JSONArray().put(JSONObject().put("key", "seat").put("value", "12"))),
                )
            }

        assertNull(pass.semantics)
        assertNull(pass.primaryFields.single().semantics)
    }

    @Test
    fun `field semantics survive storage`() {
        val field = FieldParser.parse(JSONObject().put("key", "seat").put("value", "12").put("semantics", fieldSemantics))

        assertEquals(field, PassField.fromJson(field.toJson()))
    }

    private fun parse(configure: (JSONObject) -> JSONObject) =
        PassParser().parse(
            configure(
                JSONObject()
                    .put("formatVersion", 1)
                    .put("description", "Example ticket")
                    .put("organizationName", "Example venue")
                    .put("serialNumber", "semantics-test"),
            ),
            images = PassImages(emptyMap()),
        )
}
