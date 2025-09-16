package nz.eloque.foss_wallet.persistence.loader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.graphics.createBitmap
import nz.eloque.foss_wallet.model.OriginalPass
import nz.eloque.foss_wallet.model.PassLocalization
import nz.eloque.foss_wallet.model.PassWithLocalization
import nz.eloque.foss_wallet.parsing.LocalizationParser
import nz.eloque.foss_wallet.parsing.PassParser
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.time.Instant
import java.util.zip.ZipInputStream

data class PassLoadResult(
    val pass: PassWithLocalization,
    val bitmaps: PassBitmaps,
    val originalPass: OriginalPass
)

class PassBitmaps(
    val icon: Bitmap,
    val logo: Bitmap?,
    val strip: Bitmap?,
    val thumbnail: Bitmap?,
    val footer: Bitmap?
) {

    fun saveToDisk(context: Context, id: String) {
        val directory = File(context.filesDir, id)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        save(directory, "icon.png", icon)
        save(directory, "logo.png", logo)
        save(directory, "strip.png", strip)
        save(directory, "thumbnail.png", thumbnail)
        save(directory, "footer.png", footer)
    }

    private fun save(directory: File, path: String, bitmap: Bitmap?) {
        bitmap?.let {
            FileOutputStream(File(directory, path)).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
    }
}

class PassLoader(
    private val passParser: PassParser
) {

    fun load(bytes: ByteArray, resultingId: String? = null, addedAt: Instant = Instant.now()): PassLoadResult {
        try {
            return loadPass(bytes, resultingId, addedAt)
        } catch (e: Exception) {
            throw InvalidPassException(e)
        }
    }

    private fun loadPass(bytes: ByteArray, resultingId: String? = null, addedAt: Instant): PassLoadResult {
        val localizations: MutableSet<PassLocalization> = HashSet()
        var passJson: JSONObject? = null
        var logo: Bitmap? = null
        var icon: Bitmap? = null
        var strip: Bitmap? = null
        var thumbnail: Bitmap? = null
        var footer: Bitmap? = null
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            do {
                if (!entry.isDirectory) {
                    Log.d(TAG, "Found file: ${entry.name}")
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    val baos = ByteArrayOutputStream()

                    while (zip.read(buffer).also { bytesRead = it } != -1) {
                        baos.write(buffer, 0, bytesRead)
                    }
                    val passBytes = baos.toByteArray()
                    when (entry.name) {
                        "pass.json" -> {
                            val content = passBytes.toString(detectEncoding(passBytes))
                            passJson = JsonLoader.load(content)
                            println("Content:\n$content")
                        }
                        in Regex("logo@?.*\\.png") -> {
                            logo = chooseBetter(logo, loadImage(baos))
                        }
                        in Regex("icon@?.*\\.png") -> {
                            icon = chooseBetter(icon, loadImage(baos))
                        }
                        in Regex("strip@?.*\\.png") -> {
                            strip = chooseBetter(strip, loadImage(baos))
                        }
                        in Regex("thumbnail@?.*\\.png") -> {
                            thumbnail = chooseBetter(thumbnail, loadImage(baos))
                        }
                        in Regex("footer@?.*\\.png") -> {
                            footer = chooseBetter(footer, loadImage(baos))
                        }
                        in Regex("..\\.lproj/pass.strings") -> {
                            localizations.addAll(parseLocalization(entry.name.substring(0, 2), baos))
                        }
                    }
                }
            } while (zip.nextEntry.also { entry = it } != null)
        }
        if (icon == null) {
            icon = logo ?: createBitmap(100, 100)
        }
        //TODO check signature before returning
        if (passJson != null) {
            val bitmaps = PassBitmaps(icon, logo, strip, thumbnail, footer)
            val pass = passParser.parse(passJson, resultingId, bitmaps, addedAt = addedAt)
            return PassLoadResult(PassWithLocalization(pass, localizations.toList()), bitmaps, OriginalPass(bytes))
        } else {
            throw InvalidPassException()
        }
    }

    operator fun Regex.contains(text: CharSequence): Boolean = this.matches(text)

    private fun loadImage(baos: ByteArrayOutputStream): Bitmap? {
        val array = baos.toByteArray()
        val image = BitmapFactory.decodeByteArray(array, 0, array.size)
        return if (image == null) {
            Log.w(TAG, "Failed parsing image from pkpass! Is it missing?")
            null
        } else {
            image
        }
    }

    private fun parseLocalization(lang: String, baos: ByteArrayOutputStream): Set<PassLocalization> {
        val bytes = baos.toByteArray()
        val content = bytes.toString(detectEncoding(bytes))
        return LocalizationParser.parseStrings(lang, content)
    }

    fun detectEncoding(bytes: ByteArray): Charset {
        return when {
            bytes.startsWith(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())) -> Charset.forName("UTF-8")
            bytes.startsWith(byteArrayOf(0xFF.toByte(), 0xFE.toByte())) -> Charset.forName("UTF-16LE")
            bytes.startsWith(byteArrayOf(0xFE.toByte(), 0xFF.toByte())) -> Charset.forName("UTF-16BE")
            else -> Charset.forName("UTF-8") // fallback (could be wrong, but UTF-8 is common)
        }
    }

    private fun chooseBetter(left: Bitmap?, right: Bitmap?): Bitmap? {
        return when {
            left == null && right == null -> null
            left == null -> right
            right == null -> left
            left.pixels() > right.pixels() -> left
            else -> right
        }
    }

    private fun Bitmap.pixels(): Int {
        return this.height * this.width
    }

    private fun ByteArray.startsWith(prefix: ByteArray): Boolean {
        if (this.size < prefix.size) return false
        return prefix.indices.all { this[it] == prefix[it] }
    }

    companion object {
        private const val TAG = "PassLoader"
    }
}
