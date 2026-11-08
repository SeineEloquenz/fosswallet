package nz.eloque.foss_wallet.parsing

import nz.eloque.foss_wallet.PASSES
import nz.eloque.foss_wallet.loadJson
import nz.eloque.foss_wallet.model.PassImages
import nz.eloque.foss_wallet.persistence.loader.JsonLoader
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.UUID

@RunWith(Parameterized::class)
class PassParserTest(
    private val passName: String,
) {
    private val parser = PassParser()
    private val images = PassImages(emptyMap())

    @Test
    fun testPass() {
        val jsonString = loadJson(passName)
        Assert.assertNotNull(jsonString)
        val json = JsonLoader.load(jsonString!!)
        Assert.assertNotNull(parser.parse(json, images = images))
    }

    @Test
    fun keepsGivenDeviceId() {
        val json = JsonLoader.load(loadJson(passName)!!)
        val deviceId = UUID.randomUUID()
        Assert.assertEquals(deviceId, parser.parse(json, images = images, deviceId = deviceId).deviceId)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): List<Array<String>> = PASSES.map { pass -> Array(1) { pass } }
    }
}
