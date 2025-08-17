package nz.eloque.foss_wallet.parsing

import nz.eloque.foss_wallet.persistence.loader.PassBitmaps
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.Mockito
import java.io.File
import java.nio.charset.StandardCharsets

val passes: List<String> = listOf(
    "deutschlandticket",
    "europapark",
    "eurostar",
    "ksc",
    "swiss",
    "ticketpay",
    "vector"
)

@RunWith(Parameterized::class)
class PassParserTest(private val passName: String) {

    private val parser = PassParser()
    private val bitmaps = Mockito.mock(PassBitmaps::class.java)

    @Test
    fun testPass() {
        val jsonString = loadJson(passName)
        Assert.assertNotNull(jsonString)
        val json = JSONObject(loadJson(passName)!!)
        Assert.assertNotNull(parser.parse(json, bitmaps = bitmaps))
    }

    private fun loadJson(passName: String): String? {
        val file = File("src/test/res/$passName.json")
        return file.inputStream().bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
    }
    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): List<Array<String>> {
            return passes.map { pass -> Array<String>(1) { pass } }
        }
    }
}