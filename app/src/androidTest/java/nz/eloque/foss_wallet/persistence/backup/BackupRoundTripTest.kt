package nz.eloque.foss_wallet.persistence.backup

import androidx.compose.ui.graphics.Color
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.zxing.BarcodeFormat
import kotlinx.coroutines.test.runTest
import nz.eloque.foss_wallet.model.Attachment
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassGroup
import nz.eloque.foss_wallet.model.PassLocalization
import nz.eloque.foss_wallet.model.PassMetadata
import nz.eloque.foss_wallet.model.PassTagCrossRef
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.model.Tag
import nz.eloque.foss_wallet.persistence.TransactionalExecutor
import nz.eloque.foss_wallet.persistence.WalletDb
import nz.eloque.foss_wallet.persistence.localization.PassLocalizationRepository
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class BackupRoundTripTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun newDb(): WalletDb =
        Room
            .inMemoryDatabaseBuilder(context, WalletDb::class.java)
            .allowMainThreadQueries()
            .build()

    private fun executor(db: WalletDb) =
        object : TransactionalExecutor {
            override suspend fun <T> runTransactionally(callable: suspend () -> T): T = db.withTransaction { callable() }
        }

    private fun pass(id: String) =
        Pass(
            id = id,
            description = "Pass $id",
            formatVersion = 1,
            organization = "Org",
            serialNumber = "SN-$id",
            type = PassType.Generic,
            barCodes = setOf(BarCode(BarcodeFormat.QR_CODE, "msg-$id", Charsets.UTF_8, null)),
            addedAt = Instant.ofEpochMilli(1_700_000_000_000),
        )

    private fun writeFile(
        passId: String,
        rel: String,
        bytes: ByteArray,
    ) {
        val file = File(File(context.filesDir, passId), rel)
        file.parentFile?.mkdirs()
        file.writeBytes(bytes)
    }

    @After
    fun cleanUp() {
        listOf("p1", "p2", "p3").forEach { File(context.filesDir, it).deleteRecursively() }
    }

    @Test
    fun fullRoundTripPreservesEverythingAndSkipsDuplicates() =
        runTest {
            val source = newDb()
            val sourceDao = source.passDao()
            // Two passes share a group; a third is archived and standalone.
            val groupId = sourceDao.insert(PassGroup())
            val p1 = pass("p1")
            val p2 = pass("p2")
            val p3 = pass("p3")
            sourceDao.insert(p1)
            sourceDao.insert(PassMetadata(p1.id, groupId = groupId, archived = false, autoArchive = true, renderLegacy = true))
            sourceDao.insert(p2)
            sourceDao.insert(PassMetadata(p2.id, groupId = groupId))
            sourceDao.insert(p3)
            sourceDao.insert(PassMetadata(p3.id, archived = true, autoArchive = false))

            source.tagDao().insert(Tag("travel", Color(0xFF00FF00)))
            sourceDao.tag(PassTagCrossRef(p1.id, "travel"))
            source.localizationDao().insert(PassLocalization(p1.id, "en", "Label", "Text"))
            sourceDao.insert(Attachment("ticket.pdf", p1.id))

            writeFile(p1.id, "icon.png", byteArrayOf(1, 2, 3, 4))
            writeFile(p1.id, "attachments/ticket.pdf", byteArrayOf(9, 8, 7))

            val bytes = ByteArrayOutputStream()
            BackupExporter(context, sourceDao, source.tagDao()).export(bytes)
            source.close()

            // Wipe the on-disk files to simulate restoring onto a fresh install.
            listOf(p1.id, p2.id, p3.id).forEach { File(context.filesDir, it).deleteRecursively() }

            val target = newDb()
            val targetDao = target.passDao()
            val importer =
                BackupImporter(
                    context,
                    targetDao,
                    target.tagDao(),
                    PassLocalizationRepository(target.localizationDao()),
                    executor(target),
                )

            val result = importer.import(ByteArrayInputStream(bytes.toByteArray()))
            assertEquals(BackupImportResult.Success(imported = 3, skipped = 0, failed = 0), result)

            val r1 = targetDao.findById(p1.id)
            val r2 = targetDao.findById(p2.id)
            val r3 = targetDao.findById(p3.id)
            assertNotNull(r1)
            assertNotNull(r2)
            assertNotNull(r3)
            assertEquals(p1, r1!!.pass)
            assertEquals(p3, r3!!.pass)
            // Metadata fidelity.
            assertTrue(r1.metadata.renderLegacy)
            assertTrue(r3.metadata.archived)
            // Group remapping: p1 and p2 land in the same (newly created) group.
            assertNotNull(r1.metadata.groupId)
            assertEquals(r1.metadata.groupId, r2!!.metadata.groupId)
            // Tags, localizations, attachments.
            assertEquals(listOf("travel"), r1.tags.map { it.label })
            assertEquals(listOf("Text"), r1.localizations.map { it.text })
            assertEquals(listOf("ticket.pdf"), r1.attachments.map { it.fileName })
            // Binary files restored.
            assertArrayEquals(byteArrayOf(1, 2, 3, 4), File(File(context.filesDir, p1.id), "icon.png").readBytes())
            assertArrayEquals(byteArrayOf(9, 8, 7), File(File(context.filesDir, p1.id), "attachments/ticket.pdf").readBytes())

            // Re-importing the same backup skips everything.
            val second = importer.import(ByteArrayInputStream(bytes.toByteArray()))
            assertEquals(BackupImportResult.Success(imported = 0, skipped = 3, failed = 0), second)

            target.close()
        }

    @Test
    fun invalidInputReportsInvalid() =
        runTest {
            val db = newDb()
            val importer =
                BackupImporter(
                    context,
                    db.passDao(),
                    db.tagDao(),
                    PassLocalizationRepository(db.localizationDao()),
                    executor(db),
                )
            val result = importer.import(ByteArrayInputStream("not a zip".toByteArray()))
            assertEquals(BackupImportResult.Invalid, result)
            db.close()
        }
}
