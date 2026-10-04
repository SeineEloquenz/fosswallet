package nz.eloque.foss_wallet.persistence.loader

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LoaderTest {
    private val loader = Loader(ApplicationProvider.getApplicationContext())

    @Test
    fun `zip without a pass is rejected`() {
        assertThrows(UnknownInputException::class.java) { loader.load(zipOf("readme.txt")) }
    }

    @Test
    fun `bytes that are not a zip are rejected`() {
        assertThrows(InvalidInputException::class.java) { loader.load("not a pass".toByteArray()) }
    }

    private fun zipOf(vararg names: String): ByteArray {
        val bytes = ByteArrayOutputStream()
        ZipOutputStream(bytes).use { zip ->
            names.forEach {
                zip.putNextEntry(ZipEntry(it))
                zip.write("content".toByteArray())
                zip.closeEntry()
            }
        }
        return bytes.toByteArray()
    }
}
