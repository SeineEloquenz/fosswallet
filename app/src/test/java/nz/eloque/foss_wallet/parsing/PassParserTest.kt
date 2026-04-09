package nz.eloque.foss_wallet.parsing

import nz.eloque.foss_wallet.PASSES
import nz.eloque.foss_wallet.loadJson
import nz.eloque.foss_wallet.persistence.loader.JsonLoader
import nz.eloque.foss_wallet.persistence.loader.PassBitmaps
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.Mockito

@RunWith(Parameterized::class)
class PassParserTest(
    private val passName: String,
) {
    private val parser = PassParser()
    private val bitmaps = Mockito.mock(PassBitmaps::class.java)

    @Test
    fun testPass() {
        val jsonString = loadJson(passName)
        Assert.assertNotNull(jsonString)
        val json = JsonLoader.load(jsonString!!)
        Assert.assertNotNull(parser.parse(json, bitmaps = bitmaps))
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): List<Array<String>> = PASSES.map { pass -> Array(1) { pass } }
    }
}
