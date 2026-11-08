package nz.eloque.foss_wallet.parsing

import nz.eloque.foss_wallet.model.PassImages
import nz.eloque.foss_wallet.model.PassLocation
import nz.eloque.foss_wallet.persistence.TypeConverters
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LocationsTest {
    @Test
    fun `parses locations with altitude, relevantText and maxDistance`() {
        val pass =
            parse {
                it
                    .put("maxDistance", 150)
                    .put(
                        "locations",
                        JSONArray()
                            .put(location(52.52, 13.405).put("altitude", 34.5).put("relevantText", "Gate A"))
                            .put(location(-36.85, 174.76)),
                    )
            }

        assertEquals(
            listOf(PassLocation(52.52, 13.405, 34.5, "Gate A"), PassLocation(-36.85, 174.76)),
            pass.locations,
        )
        assertEquals(150.0, pass.maxDistance)
    }

    @Test
    fun `has no locations or maxDistance by default`() {
        val pass = parse { it }

        assertEquals(emptyList<PassLocation>(), pass.locations)
        assertNull(pass.maxDistance)
    }

    @Test
    fun `treats null altitude and maxDistance as missing`() {
        val pass =
            parse {
                it
                    .put("maxDistance", JSONObject.NULL)
                    .put("locations", JSONArray().put(location(52.52, 13.405).put("altitude", JSONObject.NULL)))
            }

        assertEquals(listOf(PassLocation(52.52, 13.405)), pass.locations)
        assertNull(pass.maxDistance)
    }

    @Test
    fun `stored locations survive a round trip`() {
        val converters = TypeConverters()
        val locations = listOf(PassLocation(52.52, 13.405, 34.5, "Gate A"), PassLocation(-36.85, 174.76))

        assertEquals(locations, converters.toLocations(converters.fromLocations(locations)))
    }

    @Test
    fun `reads locations stored without altitude and relevantText`() {
        val stored = """[{"latitude":52.52,"longitude":13.405}]"""

        assertEquals(listOf(PassLocation(52.52, 13.405)), TypeConverters().toLocations(stored))
    }

    private fun location(
        latitude: Double,
        longitude: Double,
    ) = JSONObject().put("latitude", latitude).put("longitude", longitude)

    private fun parse(configure: (JSONObject) -> JSONObject) =
        PassParser().parse(
            configure(
                JSONObject()
                    .put("formatVersion", 1)
                    .put("description", "Example ticket")
                    .put("organizationName", "Example venue")
                    .put("serialNumber", "locations-test")
                    .put("eventTicket", JSONObject()),
            ),
            images = PassImages(emptyMap()),
        )
}
