package nz.eloque.foss_wallet.persistence.loader

import nz.eloque.foss_wallet.PASSES
import nz.eloque.foss_wallet.loadJson
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized


@RunWith(Parameterized::class)
class JsonLoaderTest(private val passName: String) {

    @Test
    fun testPass() {
        val jsonString = loadJson(passName)
        Assert.assertNotNull(jsonString)
        val json = JsonLoader.load(jsonString!!)
        Assert.assertNotNull(json) //Failures would throw an exception
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): List<Array<String>> {
            return PASSES.map { pass -> Array(1) { pass } }
        }
    }
}