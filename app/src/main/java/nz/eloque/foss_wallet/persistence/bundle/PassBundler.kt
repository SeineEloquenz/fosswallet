package nz.eloque.foss_wallet.persistence.bundle

import android.content.Context
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.Tag
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class PassBundler(
    val context: Context,
) {
    fun bundle(
        passes: Collection<Pass>,
        tagsByPass: Map<String, Set<Tag>> = emptyMap(),
    ): File {
        val passFiles: Map<String, File> =
            passes
                .toList()
                .filter { it.originalPassFile(context) != null }
                .associate { pass -> Pair(pass.id, pass.originalPassFile(context)!!) }
        return bundleFiles(passFiles, tagsByPass.filterKeys { it in passFiles.keys })
    }

    private fun bundleFiles(
        pkpassFiles: Map<String, File>,
        tagsByPass: Map<String, Set<Tag>>,
    ): File {
        val outputFile = File(context.cacheDir, "bundle.pkpasses")

        ZipOutputStream(FileOutputStream(outputFile)).use { zos ->
            pkpassFiles.forEach { (id, file) ->
                val entry = ZipEntry("$id.pkpass")
                zos.putNextEntry(entry)
                file.inputStream().use { input ->
                    input.copyTo(zos)
                }
                zos.closeEntry()
            }
            if (tagsByPass.values.any { it.isNotEmpty() }) {
                zos.putNextEntry(ZipEntry(TagsSerializer.BUNDLE_ENTRY))
                zos.write(TagsSerializer.serialize(tagsByPass).toByteArray())
                zos.closeEntry()
            }
        }

        return outputFile
    }
}
