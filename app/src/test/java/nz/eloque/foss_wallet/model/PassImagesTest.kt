package nz.eloque.foss_wallet.model

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PassImagesTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun image(path: String): File = File(folder.root, path).also { it.parentFile!!.mkdirs() }.also { it.writeText("") }

    @Test
    fun `saving an updated pass replaces old images and keeps other files`() {
        val directory = File(context.filesDir, "pass-1")
        val png = byteArrayOf(1)
        PassImages(mapOf("icon" to png, "strip" to png), mapOf("de" to mapOf("logo" to png))).saveToDisk(context, "pass-1")
        listOf(OriginalPass.FILE_PATH, "attachments/ticket.pdf").forEach {
            File(directory, it).apply { parentFile!!.mkdirs() }.writeText("")
        }

        PassImages(mapOf("icon" to png), mapOf("fr" to mapOf("logo" to png))).saveToDisk(context, "pass-1")

        assertFalse(File(directory, "de.lproj").exists())
        assertFalse(File(directory, "strip.png").exists())
        assertTrue(File(directory, "icon.png").exists())
        assertTrue(File(directory, "fr.lproj/logo.png").exists())
        assertTrue(File(directory, OriginalPass.FILE_PATH).exists())
        assertTrue(File(directory, "attachments/ticket.pdf").exists())
    }

    @Test
    fun `localized images override top level ones`() {
        image("logo.png")
        val german = image("de.lproj/logo.png")

        assertEquals(german, resolveImage(folder.root, "logo", "de-DE"))
    }

    @Test
    fun `matches scripted localizations case-insensitively`() {
        image("en.lproj/logo.png")
        val chinese = image("ZH-hans.lproj/logo.png")

        assertEquals(chinese, resolveImage(folder.root, "logo", "zh-Hans-CN"))
    }

    @Test
    fun `falls back to the top level image before english`() {
        val root = image("logo.png")
        image("en.lproj/logo.png")

        assertEquals(root, resolveImage(folder.root, "logo", "de-DE"))
    }

    @Test
    fun `falls back to english, then to any localization`() {
        val english = image("en.lproj/logo.png")
        val french = image("fr.lproj/logo.png")

        assertEquals(english, resolveImage(folder.root, "logo", "de-DE"))

        english.delete()
        assertEquals(french, resolveImage(folder.root, "logo", "de-DE"))
    }

    @Test
    fun `ignores localizations without the image`() {
        image("de.lproj/strip.png")
        val root = image("logo.png")

        assertEquals(root, resolveImage(folder.root, "logo", "de-DE"))
    }
}
