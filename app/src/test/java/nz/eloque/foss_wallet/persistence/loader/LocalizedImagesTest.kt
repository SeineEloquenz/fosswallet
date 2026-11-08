package nz.eloque.foss_wallet.persistence.loader

import androidx.core.graphics.createBitmap
import nz.eloque.foss_wallet.model.toPng
import nz.eloque.foss_wallet.parsing.PassParser
import org.json.JSONObject
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LocalizedImagesTest {
    private val loader = PassLoader(PassParser())

    @Test
    fun `loads images from localization folders`() {
        val result =
            loader.load(
                pkpass(
                    "icon.png",
                    "en.lproj/logo.png",
                    "de.lproj/logo@2x.png",
                    "de.lproj/strip.png",
                    "de.lproj/pass.strings",
                ),
            )

        assertEquals(setOf("en", "de"), result.images.localized.keys)
        assertEquals(setOf("logo"), result.images.localized["en"]!!.keys)
        assertEquals(setOf("logo", "strip"), result.images.localized["de"]!!.keys)
        assertNull(result.images.images["logo"])
        assertTrue(result.pass.pass.hasLogo)
        assertTrue(result.pass.pass.hasStrip)
        assertFalse(result.pass.pass.hasThumbnail)
    }

    @Test
    fun `stores localization folders with underscores as language tags`() {
        val result = loader.load(pkpass("icon.png", "zh_Hans.lproj/logo.png"))

        assertEquals(setOf("zh-Hans"), result.images.localized.keys)
    }

    @Test
    fun `ignores image names outside the pass image set`() {
        val result = loader.load(pkpass("icon.png", "en.lproj/other.png"))

        assertTrue(result.images.localized.isEmpty())
    }

    @Test
    fun `keeps the largest version of a top level image`() {
        val large = png(8)
        val result = loader.load(pkpass(contents = mapOf("logo.png" to png(4), "logo@2x.png" to large, "logo@3x.png" to png(2))))

        assertArrayEquals(large, result.images.images["logo"])
    }

    @Test
    fun `skips localized images that are not valid`() {
        val result = loader.load(pkpass("icon.png", contents = mapOf("de.lproj/logo.png" to "not a png".toByteArray())))

        assertTrue(result.images.localized.isEmpty())
        assertFalse(result.pass.pass.hasLogo)
    }

    private fun pkpass(
        vararg files: String,
        contents: Map<String, ByteArray> = emptyMap(),
    ): ByteArray {
        val passJson =
            JSONObject()
                .put("formatVersion", 1)
                .put("description", "Example ticket")
                .put("organizationName", "Example venue")
                .put("serialNumber", "localized-images-test")
                .put("eventTicket", JSONObject())
        val bytes = ByteArrayOutputStream()
        ZipOutputStream(bytes).use { zip ->
            zip.putNextEntry(ZipEntry("pass.json"))
            zip.write(passJson.toString().toByteArray())
            files.forEach { name ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(if (name.endsWith(".png")) png() else "\"key\" = \"value\";".toByteArray())
            }
            contents.forEach { (name, content) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(content)
            }
        }
        return bytes.toByteArray()
    }

    private fun png(size: Int = 4): ByteArray = createBitmap(size, size).toPng()
}
