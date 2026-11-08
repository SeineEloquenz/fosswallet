package nz.eloque.foss_wallet.persistence

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import nz.eloque.foss_wallet.api.PassbookApi
import nz.eloque.foss_wallet.api.UpdateScheduler
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassMetadata
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.model.Tag
import nz.eloque.foss_wallet.notifications.NotificationService
import nz.eloque.foss_wallet.parsing.PassParser
import nz.eloque.foss_wallet.persistence.loader.PassLoader
import nz.eloque.foss_wallet.persistence.localization.PassLocalizationRepository
import nz.eloque.foss_wallet.persistence.pass.PassRepository
import nz.eloque.foss_wallet.shortcut.ShortcutService
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackfillTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val addedAt = Instant.parse("2025-06-01T12:00:00Z")
    private val deviceId = UUID.fromString("00000000-0000-0000-0000-000000000001")

    private lateinit var db: WalletDb
    private lateinit var passRepository: PassRepository
    private lateinit var passStore: PassStore

    @Before
    fun setUp() {
        db =
            Room
                .inMemoryDatabaseBuilder(context, WalletDb::class.java)
                .setDriver(AndroidSQLiteDriver())
                .build()
        passRepository = PassRepository(context, db.passDao())
        passStore =
            PassStore(
                context,
                object : TransactionalExecutor {
                    override suspend fun <T> runTransactionally(callable: suspend () -> T): T = callable()
                },
                mock(NotificationService::class.java),
                passRepository,
                PassLocalizationRepository(db.localizationDao()),
                mock(UpdateScheduler::class.java),
                mock(ShortcutService::class.java),
                mock(PassbookApi::class.java),
            )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `re-parses stored passes and keeps their identity and metadata`() =
        runTest {
            val loaded = PassLoader(PassParser()).load(pkpass(), "stored", addedAt, deviceId)
            val stale = loaded.pass.pass.copy(maxDistance = null, voided = false)
            passRepository.insert(stale, loaded.images, loaded.originalPass)
            passRepository.archive(stale)
            val tag = Tag("Travel", Color.Red)
            db.tagDao().insert(tag)
            passRepository.tag(stale, tag)

            passStore.backfill()

            val backfilled = db.passDao().findById("stored")!!
            assertEquals(150.0, backfilled.pass.maxDistance)
            assertTrue(backfilled.pass.voided)
            assertEquals(addedAt, backfilled.pass.addedAt)
            assertEquals(deviceId, backfilled.pass.deviceId)
            assertTrue(backfilled.metadata.archived)
            assertEquals(listOf(tag), backfilled.tags)
        }

    @Test
    fun `leaves passes without an original untouched`() =
        runTest {
            val created =
                Pass(
                    id = "created",
                    description = "Created in the app",
                    formatVersion = 1,
                    organization = "Me",
                    serialNumber = "1",
                    type = PassType.Generic,
                    barCodes = setOf(),
                    addedAt = addedAt,
                )
            db.passDao().insert(created)
            db.passDao().insert(PassMetadata(created.id))

            passStore.backfill()

            assertEquals(created, db.passDao().findById("created")!!.pass)
            assertNull(created.originalPassFile(context))
        }

    private fun pkpass(): ByteArray {
        val passJson =
            JSONObject()
                .put("formatVersion", 1)
                .put("description", "Example ticket")
                .put("organizationName", "Example venue")
                .put("serialNumber", "backfill-test")
                .put("maxDistance", 150)
                .put("voided", true)
                .put("eventTicket", JSONObject())
        val bytes = ByteArrayOutputStream()
        ZipOutputStream(bytes).use { zip ->
            zip.putNextEntry(ZipEntry("pass.json"))
            zip.write(passJson.toString().toByteArray())
            zip.closeEntry()
        }
        return bytes.toByteArray()
    }
}
