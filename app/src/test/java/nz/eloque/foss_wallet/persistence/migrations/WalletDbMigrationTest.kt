package nz.eloque.foss_wallet.persistence.migrations

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import nz.eloque.foss_wallet.model.PassRelevantDate
import nz.eloque.foss_wallet.model.field.PassContent
import nz.eloque.foss_wallet.persistence.TypeConverters
import nz.eloque.foss_wallet.persistence.buildDb
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WalletDbMigrationTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val dbFile: File = context.getDatabasePath("wallet_db")

    @Before
    fun setUp() {
        dbFile.parentFile!!.mkdirs()
        SQLiteDatabase.deleteDatabase(dbFile)
    }

    @After
    fun tearDown() {
        SQLiteDatabase.deleteDatabase(dbFile)
    }

    @Test
    fun `v14 dates and fields are converted on the way to the current schema`() =
        runTest {
            withSchema(14) {
                insertPass("zeroed", relevantDate = 0, expirationDate = 0)
                insertPass(
                    "dated",
                    relevantDate = 1_700_000_000,
                    expirationDate = 1_800_000_000,
                    primaryFields = fields("${PassContent.DATE}|2013-04-24T10:00-05:00|MEDIUM"),
                )
            }

            migrateToCurrent()

            query("SELECT relevantDates, expirationDate FROM Pass WHERE id = 'zeroed'") {
                assertEquals(emptyList<PassRelevantDate>(), TypeConverters().toRelevantDates(getString(0)))
                assertNull(getString(1))
            }
            query("SELECT relevantDates, expirationDate, primaryFields FROM Pass WHERE id = 'dated'") {
                assertEquals(
                    listOf(Instant.ofEpochSecond(1_700_000_000)),
                    TypeConverters().toRelevantDates(getString(0)).map { (it as PassRelevantDate.Date).date.toInstant() },
                )
                assertEquals(Instant.ofEpochSecond(1_800_000_000), TypeConverters().toZonedDateTime(getString(1)).toInstant())
                assertEquals(
                    "${PassContent.DATE}|2013-04-24T10:00-05:00|MEDIUM|false",
                    JSONArray(getString(2)).getJSONObject(0).getString("value"),
                )
            }
            query("SELECT COUNT(*) FROM PassMetadata") { assertEquals(2, getInt(0)) }
        }

    @Test
    fun `v9 integer ids become text ids`() =
        runTest {
            withSchema(9) {
                insertPass("7", relevantDate = 0, expirationDate = 0)
                execSQL("INSERT INTO localization (passId, lang, label, text) VALUES (7, 'de', 'gate', 'Tor')")
            }

            migrateToCurrent()

            query("SELECT id, typeof(id) FROM Pass") {
                assertEquals("7", getString(0))
                assertEquals("text", getString(1))
            }
            query("SELECT passId, typeof(passId), text FROM localization") {
                assertEquals("7", getString(0))
                assertEquals("text", getString(1))
                assertEquals("Tor", getString(2))
            }
        }

    private fun withSchema(
        version: Int,
        seed: SQLiteDatabase.() -> Unit,
    ) {
        val schema =
            JSONObject(File("schemas/nz.eloque.foss_wallet.persistence.WalletDb/$version.json").readText())
                .getJSONObject("database")
        SQLiteDatabase.openOrCreateDatabase(dbFile, null).use { db ->
            val entities = schema.getJSONArray("entities")
            for (i in 0 until entities.length()) {
                val entity = entities.getJSONObject(i)
                val tableName = entity.getString("tableName")
                db.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", tableName))
                val indices = entity.optJSONArray("indices") ?: JSONArray()
                for (j in 0 until indices.length()) {
                    db.execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}", tableName))
                }
            }
            val setupQueries = schema.getJSONArray("setupQueries")
            for (i in 0 until setupQueries.length()) db.execSQL(setupQueries.getString(i))
            db.version = version
            db.seed()
        }
    }

    private suspend fun migrateToCurrent() {
        val walletDb = buildDb(context)
        withContext(Dispatchers.IO) { walletDb.tagDao().crossRef() }
        walletDb.close()
    }

    private fun query(
        sql: String,
        assert: Cursor.() -> Unit,
    ) {
        SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY).use { db ->
            db.rawQuery(sql, null).use { cursor ->
                assertEquals(1, cursor.count)
                cursor.moveToFirst()
                cursor.assert()
            }
        }
    }

    private fun SQLiteDatabase.insertPass(
        id: String,
        relevantDate: Long,
        expirationDate: Long,
        primaryFields: String = "[]",
    ) = execSQL(
        """
        INSERT INTO Pass (
            id, description, formatVersion, organization, serialNumber, type, barCodes,
            hasLogo, hasStrip, hasThumbnail, hasFooter, relevantDate, expirationDate, locations,
            headerFields, primaryFields, secondaryFields, auxiliaryFields, backFields
        ) VALUES (?, 'Pass', 1, 'Org', '1', 'GENERIC', '[]', 0, 0, 0, 0, ?, ?, '[]', '[]', ?, '[]', '[]', '[]')
        """.trimIndent(),
        arrayOf<Any>(id, relevantDate, expirationDate, primaryFields),
    )

    private fun fields(value: String) = JSONArray().put(JSONObject().put("key", "date").put("value", value)).toString()
}
