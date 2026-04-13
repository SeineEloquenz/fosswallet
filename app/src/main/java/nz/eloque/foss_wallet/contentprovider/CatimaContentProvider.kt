package nz.eloque.foss_wallet.contentprovider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.util.Log
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import nz.eloque.foss_wallet.BuildConfig
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassTagCrossRef
import nz.eloque.foss_wallet.model.Tag
import nz.eloque.foss_wallet.persistence.SettingsStore
import nz.eloque.foss_wallet.persistence.pass.PassRepository
import nz.eloque.foss_wallet.persistence.tag.TagRepository
import java.time.Instant

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ProviderEntrypoint {
    fun passRepository(): PassRepository

    fun settingsStore(): SettingsStore

    fun tagRepository(): TagRepository
}

class CatimaContentProvider : ContentProvider() {
    companion object {
        private const val TAG = "CatimaContentProvider"

        const val AUTHORITY: String = BuildConfig.APPLICATION_ID + ".contentprovider.cards"

        private const val URI_VERSION = 0
        private const val URI_CARDS = 1
        private const val URI_GROUPS = 2
        private const val URI_CARD_GROUPS = 3

        private val uriMatcher: UriMatcher =
            object : UriMatcher(NO_MATCH) {
                init {
                    addURI(AUTHORITY, "version", URI_VERSION)
                    addURI(AUTHORITY, "cards", URI_CARDS)
                    addURI(AUTHORITY, "groups", URI_GROUPS)
                    addURI(AUTHORITY, "card_groups", URI_CARD_GROUPS)
                }
            }
    }

    object Version {
        const val MAJOR_COLUMN: String = "major"
        const val MINOR_COLUMN: String = "minor"
        const val MAJOR: Int = 1
        const val MINOR: Int = 1
    }

    object CatimaFields {
        const val ID: String = "_id"
        const val STORE: String = "store"
        const val VALID_FROM: String = "validfrom"
        const val EXPIRY: String = "expiry"
        const val BALANCE: String = "balance"
        const val BALANCE_TYPE: String = "balancetype"
        const val NOTE: String = "note"
        const val HEADER_COLOR: String = "headercolor"
        const val CARD_ID: String = "cardid"
        const val BARCODE_ID: String = "barcodeid"
        const val BARCODE_TYPE: String = "barcodetype"
        const val BARCODE_ENCODING: String = "barcodeencoding"
        const val STAR_STATUS: String = "starstatus"
        const val LAST_USED: String = "lastused"
        const val ARCHIVE_STATUS: String = "archive"

        val defaultProjection: Array<String> =
            arrayOf(
                ID,
                STORE,
                VALID_FROM,
                EXPIRY,
                BALANCE,
                BALANCE_TYPE,
                NOTE,
                HEADER_COLOR,
                CARD_ID,
                BARCODE_ID,
                BARCODE_TYPE,
                BARCODE_ENCODING,
                STAR_STATUS,
                LAST_USED,
                ARCHIVE_STATUS,
            )
    }

    private lateinit var passRepository: PassRepository
    private lateinit var tagRepository: TagRepository
    private lateinit var settingsStore: SettingsStore

    override fun onCreate(): Boolean {
        val entryPoint =
            EntryPointAccessors.fromApplication(
                context!!.applicationContext,
                ProviderEntrypoint::class.java,
            )
        passRepository = entryPoint.passRepository()
        tagRepository = entryPoint.tagRepository()
        settingsStore = entryPoint.settingsStore()

        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String?>?,
        selection: String?,
        selectionArgs: Array<String?>?,
        sortOrder: String?,
    ): Cursor? {
        when (uriMatcher.match(uri)) {
            URI_VERSION -> {
                return queryVersion()
            }

            URI_CARDS -> {
                val passes =
                    runBlocking(Dispatchers.IO) {
                        passRepository.all().first()
                    }

                return buildPassesCursor(passes.map { it.pass }, projection ?: CatimaFields.defaultProjection)
            }

            URI_GROUPS -> {
                val tags =
                    runBlocking(Dispatchers.IO) {
                        tagRepository.all().first()
                    }

                return buildTagsCursor(tags)
            }

            URI_CARD_GROUPS -> {
                val crossRefs =
                    runBlocking(Dispatchers.IO) {
                        tagRepository.crossRef()
                    }

                return buildCrossRefCursor(crossRefs)
            }

            else -> {
                Log.w(TAG, "Unrecognized URI $uri")
                return null
            }
        }
    }

    private fun buildPassesCursor(
        passes: List<Pass>,
        columns: Array<out String?>,
    ): Cursor {
        val cursor = MatrixCursor(columns)

        passes.forEach { pass ->
            cursor
                .newRow()
                .add(CatimaFields.ID, pass.id.hashCode())
                .add(CatimaFields.STORE, pass.organization)
                .add(
                    CatimaFields.VALID_FROM,
                    pass.relevantDates
                        .firstOrNull()
                        ?.startDate()
                        ?.toInstant()
                        ?.toEpochMilli() ?: Instant.EPOCH.toEpochMilli(),
                ).add(CatimaFields.EXPIRY, pass.expirationDate?.toInstant()?.toEpochMilli() ?: Long.MAX_VALUE)
                .add(CatimaFields.BALANCE, 0)
                .add(CatimaFields.BALANCE_TYPE, null)
                .add(CatimaFields.NOTE, pass.description)
                .add(CatimaFields.HEADER_COLOR, pass.colors?.background)
                .add(CatimaFields.CARD_ID, pass.id)
                .add(CatimaFields.BARCODE_ID, pass.barCodes.firstOrNull()?.message)
                .add(
                    CatimaFields.BARCODE_TYPE,
                    pass.barCodes
                        .firstOrNull()
                        ?.format
                        .toString(),
                ).add(
                    CatimaFields.BARCODE_ENCODING,
                    pass.barCodes
                        .firstOrNull()
                        ?.encoding
                        .toString(),
                ).add(CatimaFields.STAR_STATUS, 0)
                .add(CatimaFields.LAST_USED, 0)
                .add(CatimaFields.ARCHIVE_STATUS, if (pass.archived) 1 else 0)
        }

        return cursor
    }

    private fun buildTagsCursor(tags: Set<Tag>): Cursor {
        val cursor = MatrixCursor(arrayOf("_id", "orderId"))

        tags.sortedBy { it.label }.forEachIndexed { index, tag ->
            cursor
                .newRow()
                .add("_id", tag.label)
                .add("orderId", index)
        }
        return cursor
    }

    private fun buildCrossRefCursor(crossRefs: List<PassTagCrossRef>): Cursor {
        val cursor = MatrixCursor(arrayOf("cardId", "groupId"))

        crossRefs.forEach { crossRef ->
            cursor
                .newRow()
                .add("cardId", crossRef.passId)
                .add("groupId", crossRef.tagLabel)
        }
        return cursor
    }

    private fun queryVersion(): Cursor {
        val columns: Array<String?> = arrayOf(Version.MAJOR_COLUMN, Version.MINOR_COLUMN)
        val matrixCursor = MatrixCursor(columns)
        matrixCursor.addRow(arrayOf<Any>(Version.MAJOR, Version.MINOR))

        return matrixCursor
    }

    override fun getType(uri: Uri): String? {
        // MIME types are not relevant (for now at least)
        return null
    }

    override fun insert(
        uri: Uri,
        values: ContentValues?,
    ): Uri? {
        // This content provider is read-only for now, so we always return null
        return null
    }

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String?>?,
    ): Int {
        // This content provider is read-only for now, so we always return 0
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String?>?,
    ): Int {
        // This content provider is read-only for now, so we always return 0
        return 0
    }
}
