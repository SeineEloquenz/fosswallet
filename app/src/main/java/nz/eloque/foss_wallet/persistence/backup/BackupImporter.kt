package nz.eloque.foss_wallet.persistence.backup

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.serialization.decodeFromString
import nz.eloque.foss_wallet.model.Attachment
import nz.eloque.foss_wallet.model.PassGroup
import nz.eloque.foss_wallet.model.PassTagCrossRef
import nz.eloque.foss_wallet.persistence.TransactionalExecutor
import nz.eloque.foss_wallet.persistence.localization.PassLocalizationRepository
import nz.eloque.foss_wallet.persistence.pass.PassDao
import nz.eloque.foss_wallet.persistence.tag.TagDao
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

private const val TAG = "BackupImporter"

class BackupImporter
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        private val passDao: PassDao,
        private val tagDao: TagDao,
        private val localizationRepository: PassLocalizationRepository,
        private val transactionalExecutor: TransactionalExecutor,
    ) {
        suspend fun import(input: InputStream): BackupImportResult {
            var imported = 0
            var skipped = 0
            var failed = 0
            val groupIdMap = HashMap<Long, Long>()
            val importedIds = HashSet<String>()

            try {
                ZipInputStream(BufferedInputStream(input)).use { zip ->
                    val first = zip.nextEntry ?: return BackupImportResult.Invalid
                    if (first.name != BackupFormat.MANIFEST_FILE) return BackupImportResult.Invalid
                    val manifest =
                        try {
                            backupJson.decodeFromString<BackupManifest>(zip.readBytes().decodeToString())
                        } catch (e: Exception) {
                            Log.e(TAG, "Unreadable manifest", e)
                            return BackupImportResult.Invalid
                        }
                    if (manifest.formatVersion > BackupFormat.FORMAT_VERSION) return BackupImportResult.Invalid
                    transactionalExecutor.runTransactionally { manifest.tags.forEach { tagDao.insert(it) } }

                    var entry = zip.nextEntry
                    while (entry != null) {
                        if (!entry.isDirectory) {
                            val path = parsePath(entry.name)
                            if (path != null) {
                                val (passId, rel) = path
                                if (rel == BackupFormat.ENTRY_FILE) {
                                    val bytes = zip.readBytes()
                                    try {
                                        if (passDao.findById(passId) != null) {
                                            skipped++
                                        } else {
                                            insertEntry(
                                                backupJson.decodeFromString<BackupEntry>(bytes.decodeToString()),
                                                groupIdMap,
                                            )
                                            imported++
                                            importedIds.add(passId)
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Failed to import pass $passId", e)
                                        failed++
                                    }
                                } else if (passId in importedIds) {
                                    extractFile(passId, rel, zip)
                                }
                            }
                        }
                        entry = zip.nextEntry
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read backup", e)
                return BackupImportResult.Invalid
            }
            return BackupImportResult.Success(imported, skipped, failed)
        }

        private suspend fun insertEntry(
            entry: BackupEntry,
            groupIdMap: HashMap<Long, Long>,
        ) {
            transactionalExecutor.runTransactionally {
                passDao.insert(entry.pass)
                val newGroupId = entry.metadata.groupId?.let { backup -> groupIdMap.getOrPut(backup) { passDao.insert(PassGroup()) } }
                passDao.insert(entry.metadata.copy(groupId = newGroupId))
                entry.tagLabels.forEach { passDao.tag(PassTagCrossRef(entry.pass.id, it)) }
                entry.localizations.forEach { localizationRepository.insert(it) }
                entry.attachmentNames.forEach { passDao.insert(Attachment(fileName = it, passId = entry.pass.id)) }
            }
        }

        private fun extractFile(
            passId: String,
            rel: String,
            zip: InputStream,
        ) {
            val target = File(File(context.filesDir, passId), rel)
            // Zip-slip guard: a malicious id/path must never let us write outside filesDir.
            if (!target.canonicalPath.startsWith(context.filesDir.canonicalPath + File.separator)) {
                Log.w(TAG, "Skipping suspicious backup path: $passId/$rel")
                return
            }
            target.parentFile?.mkdirs()
            FileOutputStream(target).use { out -> zip.copyTo(out) }
        }

        /** Splits a `passes/{id}/{rel}` entry name into (id, rel); null if it is not a pass entry. */
        private fun parsePath(name: String): Pair<String, String>? {
            val prefix = "${BackupFormat.PASSES_DIR}/"
            if (!name.startsWith(prefix)) return null
            val rest = name.removePrefix(prefix)
            val slash = rest.indexOf('/')
            if (slash <= 0 || slash == rest.length - 1) return null
            return rest.substring(0, slash) to rest.substring(slash + 1)
        }
    }
