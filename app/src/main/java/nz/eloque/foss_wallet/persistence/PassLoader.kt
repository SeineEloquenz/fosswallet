package nz.eloque.foss_wallet.persistence

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.graphics.createBitmap
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassLocalization
import nz.eloque.foss_wallet.parsing.LocalizationParser
import nz.eloque.foss_wallet.parsing.PassParser
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

class InvalidPassException : Exception()

class PassBitmaps(
    val icon: Bitmap,
    val logo: Bitmap?,
    val strip: Bitmap?,
    val thumbnail: Bitmap?,
    val footer: Bitmap?
) {

    fun saveToDisk(context: Context, id: Long) {
        val directory = File(context.filesDir, "$id")
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

    fun load(inputStream: InputStream): Triple<Pass, PassBitmaps, Set<PassLocalization>> {
        val localizations: MutableSet<PassLocalization> = HashSet()
        var passJson: JSONObject? = null
        var logo: Bitmap? = null
        var icon: Bitmap? = null
        var strip: Bitmap? = null
        var thumbnail: Bitmap? = null
        var footer: Bitmap? = null
        ZipInputStream(inputStream).use { zip ->
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
                    when (entry.name) {
                        "pass.json" -> {
                            val content = baos.toString("UTF-8")
                            passJson = JSONObject(content)
                            println("Content:\n$content")
                        }
                        "logo.png", "logo@2x.png" -> {
                            logo = chooseBetter(logo, loadImage(baos))
                        }
                        "icon.png", "icon@2x.png" -> {
                            icon = chooseBetter(icon, loadImage(baos))
                        }
                        "strip.png", "strip@2x.png" -> {
                            strip = chooseBetter(strip, loadImage(baos))
                        }
                        "thumbnail.png", "thumbnail@2x.png" -> {
                            thumbnail = chooseBetter(thumbnail, loadImage(baos))
                        }
                        "footer.png", "footer@2x.png" -> {
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
            val bitmaps = PassBitmaps(icon!!, logo, strip, thumbnail, footer)
            return passParser.parse(passJson!!, bitmaps, localizations)
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
        val content = baos.toString("UTF-8")
        return LocalizationParser.parseStrings(lang, content)
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

    companion object {
        private const val TAG = "PassLoader"
    }
}
