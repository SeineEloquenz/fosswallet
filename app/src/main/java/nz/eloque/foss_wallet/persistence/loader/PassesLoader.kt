package nz.eloque.foss_wallet.persistence.loader

import android.util.Log
import nz.eloque.foss_wallet.model.Tag
import nz.eloque.foss_wallet.persistence.bundle.TagsSerializer
import nz.eloque.foss_wallet.utils.toByteArray
import java.io.ByteArrayInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

data class LoadedBundle(
    val passes: Set<PassLoadResult>,
    val tagsByPass: Map<String, Set<Tag>>,
)

class PassesLoader(
    val passLoader: PassLoader,
) {
    fun load(bytes: ByteArray): LoadedBundle =
        try {
            val results = mutableSetOf<PassLoadResult>()
            var tagsByPass: Map<String, Set<Tag>> = emptyMap()

            ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
                var entry: ZipEntry? = zip.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory && entry.name.endsWith(".pkpass", ignoreCase = true)) {
                        val passBytes = zip.toByteArray()
                        try {
                            results.add(passLoader.load(passBytes))
                        } catch (e: InvalidPassException) {
                            Log.w(TAG, "Skipping invalid pass ${entry.name} in bundle: $e")
                        }
                    } else if (!entry.isDirectory && entry.name == TagsSerializer.BUNDLE_ENTRY) {
                        try {
                            tagsByPass = TagsSerializer.deserialize(zip.toByteArray().decodeToString())
                        } catch (e: Exception) {
                            Log.w(TAG, "Skipping unreadable ${TagsSerializer.BUNDLE_ENTRY} in bundle: $e")
                        }
                    }
                    entry = zip.nextEntry
                }
            }

            LoadedBundle(results, tagsByPass)
        } catch (e: Exception) {
            throw InvalidPassesException(e)
        }

    companion object {
        private const val TAG = "PassesLoader"
    }
}
