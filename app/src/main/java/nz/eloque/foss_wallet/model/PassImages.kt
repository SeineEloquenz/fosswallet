package nz.eloque.foss_wallet.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import nz.eloque.foss_wallet.utils.bestLanguage
import java.io.ByteArrayOutputStream
import java.io.File

private const val LPROJ = ".lproj"

val IMAGE_NAMES = listOf("icon", "logo", "strip", "thumbnail", "footer", "background")

fun ByteArray.decodeBitmap(): Bitmap? = BitmapFactory.decodeByteArray(this, 0, size)

fun Bitmap.toPng(): ByteArray = ByteArrayOutputStream().also { compress(Bitmap.CompressFormat.PNG, 100, it) }.toByteArray()

/** PNG bytes of pass images by name such as `logo`, top level and by language tag. */
data class PassImages(
    val images: Map<String, ByteArray>,
    val localized: Map<String, Map<String, ByteArray>> = emptyMap(),
) {
    fun has(name: String): Boolean = first(name) != null

    fun first(name: String): ByteArray? = images[name] ?: localized.values.firstNotNullOfOrNull { it[name] }

    fun saveToDisk(
        context: Context,
        id: String,
    ) {
        val directory = File(context.filesDir, id).also { it.mkdirs() }
        IMAGE_NAMES.forEach { name ->
            val file = File(directory, "$name.png")
            images[name]?.let { file.writeBytes(it) } ?: file.delete()
        }
        directory.localizationDirectories().forEach { it.deleteRecursively() }
        localized.forEach { (language, images) ->
            val languageDirectory = File(directory, "$language$LPROJ").also { it.mkdirs() }
            images.forEach { (name, bytes) -> File(languageDirectory, "$name.png").writeBytes(bytes) }
        }
    }
}

/** Resolves a stored pass image by the matching localization, then top level, then English, then any localization. */
fun resolveImage(
    directory: File,
    name: String,
    languageTag: String,
): File? {
    val localized =
        directory
            .localizationDirectories()
            .associate { it.name.removeSuffix(LPROJ) to File(it, "$name.png") }
            .filterValues { it.exists() }
    val image = File(directory, "$name.png")
    return bestLanguage(localized.keys, languageTag)?.let { localized[it] }
        ?: image.takeIf { it.exists() }
        ?: bestLanguage(localized.keys, "en")?.let { localized[it] }
        ?: localized.values.firstOrNull()
}

private fun File.localizationDirectories(): List<File> =
    listFiles { file -> file.isDirectory && file.name.endsWith(LPROJ) }?.toList().orEmpty()
