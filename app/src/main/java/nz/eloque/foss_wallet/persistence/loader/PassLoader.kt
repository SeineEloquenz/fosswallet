package nz.eloque.foss_wallet.persistence.loader

import android.util.Log
import nz.eloque.foss_wallet.model.IMAGE_NAMES
import nz.eloque.foss_wallet.model.OriginalPass
import nz.eloque.foss_wallet.model.PassImages
import nz.eloque.foss_wallet.model.PassLocalization
import nz.eloque.foss_wallet.model.PassWithLocalization
import nz.eloque.foss_wallet.model.decodeBitmap
import nz.eloque.foss_wallet.parsing.LocalizationParser
import nz.eloque.foss_wallet.parsing.PassParser
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.util.UUID
import java.util.zip.ZipInputStream

data class PassLoadResult(
    val pass: PassWithLocalization,
    val images: PassImages,
    val originalPass: OriginalPass,
)

class PassLoader(
    private val passParser: PassParser,
) {
    fun load(
        bytes: ByteArray,
        resultingId: String? = null,
        addedAt: Instant = Instant.now(),
        deviceId: UUID = UUID.randomUUID(),
    ): PassLoadResult {
        try {
            return loadPass(bytes, resultingId, addedAt, deviceId)
        } catch (e: Exception) {
            throw InvalidPassException(e)
        }
    }

    private fun loadPass(
        bytes: ByteArray,
        resultingId: String? = null,
        addedAt: Instant,
        deviceId: UUID,
    ): PassLoadResult {
        val localizations: MutableSet<PassLocalization> = HashSet()
        var passJson: JSONObject? = null
        val images: MutableMap<String, MutableMap<String, ByteArray>> = HashMap()
        val pixels: MutableMap<Pair<String, String>, Int> = HashMap()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
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
                            passJson = JsonLoader.load(TextDecoder.decode(passBytes))
                        }

                        in IMAGE_REGEX -> {
                            val (rawLanguage, name) = IMAGE_REGEX.matchEntire(entry.name)!!.destructured
                            val language = rawLanguage.replace('_', '-')
                            val size = passBytes.decodeBitmap()?.let { it.width * it.height }
                            if (size == null) {
                                Log.w(TAG, "Failed parsing image ${entry.name}")
                            } else if (size >= (pixels[language to name] ?: 0)) {
                                pixels[language to name] = size
                                images.getOrPut(language) { HashMap() }[name] = passBytes
                            }
                        }

                        in LOCALIZATION_FILE_REGEX -> {
                            try {
                                localizations.addAll(parseLocalization(entry.name.substringBefore(".lproj"), baos))
                            } catch (e: Exception) {
                                Log.w(TAG, "Failed to parse localization ${entry.name}, skipping", e)
                            }
                        }
                    }
                }
                entry = zip.nextEntry
            }
        }
        val passImages = PassImages(images.remove("").orEmpty(), images)
        // TODO check signature before returning
        if (passJson != null) {
            val pass = passParser.parse(passJson, resultingId, passImages, addedAt = addedAt, deviceId = deviceId)
            return PassLoadResult(PassWithLocalization(pass, localizations.toList()), passImages, OriginalPass(bytes))
        } else {
            throw InvalidPassException()
        }
    }

    operator fun Regex.contains(text: CharSequence): Boolean = this.matches(text)

    private fun parseLocalization(
        lang: String,
        baos: ByteArrayOutputStream,
    ): Set<PassLocalization> = LocalizationParser.parseStrings(lang, TextDecoder.decode(baos.toByteArray()))

    companion object {
        private const val TAG = "PassLoader"

        // The language becomes a directory name on disk, so it must never admit path separators or dots.
        private const val LANGUAGE = "[A-Za-z]{2,3}(?:[-_][A-Za-z0-9]{2,8})*"
        private val LOCALIZATION_FILE_REGEX = Regex("^$LANGUAGE\\.lproj/pass\\.strings$")
        private val IMAGE_REGEX =
            Regex("^(?:($LANGUAGE)\\.lproj/)?(${IMAGE_NAMES.joinToString("|")})(?:@[^/]*)?\\.png$")
    }
}
