package nz.eloque.foss_wallet.persistence.backup

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import nz.eloque.foss_wallet.BuildConfig
import nz.eloque.foss_wallet.model.PassWithMetadata
import nz.eloque.foss_wallet.model.Tag
import nz.eloque.foss_wallet.persistence.pass.PassDao
import nz.eloque.foss_wallet.persistence.tag.TagDao
import java.io.BufferedOutputStream
import java.io.File
import java.io.OutputStream
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Writes a full-fidelity `.fosswallet` backup (ZIP) of every stored pass to [out].
 *
 * Layout: `manifest.json` first, then for each pass `passes/{id}/backup.json` immediately followed
 * by a raw copy of every file under `filesDir/{id}/`. [BackupImporter] relies on this ordering.
 */
class BackupExporter
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        private val passDao: PassDao,
        private val tagDao: TagDao,
    ) {
        suspend fun export(out: OutputStream) {
            val passes = passDao.all().first()
            val tags = tagDao.all().first()
            ZipOutputStream(BufferedOutputStream(out)).use { zip ->
                writeManifest(zip, passes, tags)
                passes.forEach { writePass(zip, it) }
            }
        }

        private fun writeManifest(
            zip: ZipOutputStream,
            passes: List<PassWithMetadata>,
            tags: List<Tag>,
        ) {
            val manifest =
                BackupManifest(
                    formatVersion = BackupFormat.FORMAT_VERSION,
                    createdAt = Instant.now().toString(),
                    appVersionName = BuildConfig.VERSION_NAME,
                    appVersionCode = BuildConfig.VERSION_CODE.toLong(),
                    tags = tags,
                    passIds = passes.map { it.pass.id },
                )
            zip.putNextEntry(ZipEntry(BackupFormat.MANIFEST_FILE))
            zip.write(backupJson.encodeToString(manifest).toByteArray())
            zip.closeEntry()
        }

        private fun writePass(
            zip: ZipOutputStream,
            pwm: PassWithMetadata,
        ) {
            val id = pwm.pass.id
            val base = "${BackupFormat.PASSES_DIR}/$id"
            val entry =
                BackupEntry(
                    pass = pwm.pass,
                    metadata = pwm.metadata,
                    tagLabels = pwm.tags.map { it.label },
                    localizations = pwm.localizations,
                    attachmentNames = pwm.attachments.map { it.fileName },
                )
            zip.putNextEntry(ZipEntry("$base/${BackupFormat.ENTRY_FILE}"))
            zip.write(backupJson.encodeToString(entry).toByteArray())
            zip.closeEntry()

            val passDir = File(context.filesDir, id)
            if (passDir.isDirectory) {
                passDir
                    .walkTopDown()
                    .filter { it.isFile }
                    .forEach { file ->
                        val rel = file.toRelativeString(passDir).replace(File.separatorChar, '/')
                        zip.putNextEntry(ZipEntry("$base/$rel"))
                        file.inputStream().use { it.copyTo(zip) }
                        zip.closeEntry()
                    }
            }
        }
    }
