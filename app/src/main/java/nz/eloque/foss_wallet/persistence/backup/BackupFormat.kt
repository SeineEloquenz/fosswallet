package nz.eloque.foss_wallet.persistence.backup

import kotlinx.serialization.Serializable
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassLocalization
import nz.eloque.foss_wallet.model.PassMetadata
import nz.eloque.foss_wallet.model.Tag

/** Constants describing the `.fosswallet` backup container layout. */
object BackupFormat {
    const val FORMAT_VERSION = 1
    const val FILE_EXTENSION = "fosswallet"
    const val MANIFEST_FILE = "manifest.json"
    const val PASSES_DIR = "passes"
    const val ENTRY_FILE = "backup.json"
}

/** Everything captured for a single pass in a backup (database side; binary files travel alongside). */
@Serializable
data class BackupEntry(
    val pass: Pass,
    val metadata: PassMetadata,
    val tagLabels: List<String> = emptyList(),
    val localizations: List<PassLocalization> = emptyList(),
    val attachmentNames: List<String> = emptyList(),
)

/** Top-level backup descriptor: format version, provenance, the global tag set, and contained pass ids. */
@Serializable
data class BackupManifest(
    val formatVersion: Int = BackupFormat.FORMAT_VERSION,
    val createdAt: String = "",
    val appVersionName: String = "",
    val appVersionCode: Long = 0,
    val tags: List<Tag> = emptyList(),
    val passIds: List<String> = emptyList(),
)
