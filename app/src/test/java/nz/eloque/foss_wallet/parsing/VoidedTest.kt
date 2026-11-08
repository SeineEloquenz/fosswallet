package nz.eloque.foss_wallet.parsing

import nz.eloque.foss_wallet.model.PassImages
import org.json.JSONObject
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VoidedTest {
    @Test
    fun `parses voided passes`() {
        assertTrue(parse { it.put("voided", true) }.voided)
    }

    @Test
    fun `passes are not voided by default`() {
        assertFalse(parse { it }.voided)
        assertFalse(parse { it.put("voided", false) }.voided)
    }

    private fun parse(configure: (JSONObject) -> JSONObject) =
        PassParser().parse(
            configure(
                JSONObject()
                    .put("formatVersion", 1)
                    .put("description", "Example coupon")
                    .put("organizationName", "Example shop")
                    .put("serialNumber", "voided-test")
                    .put("coupon", JSONObject()),
            ),
            images = PassImages(emptyMap()),
        )
}
