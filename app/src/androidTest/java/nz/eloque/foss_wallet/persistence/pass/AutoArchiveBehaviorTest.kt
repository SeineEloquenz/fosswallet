package nz.eloque.foss_wallet.persistence.pass

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.persistence.WalletDb
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.ZoneOffset

@RunWith(AndroidJUnit4::class)
class AutoArchiveBehaviorTest {
    private val fixedNow = Instant.parse("2026-01-01T00:00:00Z")
    private val fixedExpiredDate = fixedNow.minusSeconds(24 * 60 * 60).atOffset(ZoneOffset.UTC).toZonedDateTime()
    private val fixedFutureDate = fixedNow.plusSeconds(24 * 60 * 60).atOffset(ZoneOffset.UTC).toZonedDateTime()

    private lateinit var db: WalletDb
    private lateinit var passDao: PassDao
    private lateinit var passRepository: PassRepository

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, WalletDb::class.java)
            .allowMainThreadQueries()
            .build()
        passDao = db.passDao()
        passRepository = PassRepository(context, passDao)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun passIsAutoArchivedOnceButNotAfterUnarchive() {
        val pass = Pass(
            id = "expired-pass",
            description = "Expired pass",
            formatVersion = 1,
            organization = "Test Org",
            serialNumber = "123",
            type = PassType.Generic,
            barCodes = setOf(),
            addedAt = fixedNow,
            expirationDate = fixedExpiredDate,
        )
        passDao.insert(pass)

        passRepository.archiveExpiredPasses(now = fixedNow)

        val afterFirstAutoArchiveRun = passDao.findById(pass.id)?.pass
        assertNotNull(afterFirstAutoArchiveRun)
        assertTrue(afterFirstAutoArchiveRun!!.archived)
        assertTrue(afterFirstAutoArchiveRun.autoArchive)

        passDao.unarchive(pass.id)
        val afterUnarchive = passDao.findById(pass.id)?.pass
        assertNotNull(afterUnarchive)
        assertFalse(afterUnarchive!!.archived)
        assertFalse(afterUnarchive.autoArchive)

        passRepository.archiveExpiredPasses(now = fixedNow)

        val afterAutoArchiveRun = passDao.findById(pass.id)?.pass
        assertNotNull(afterAutoArchiveRun)
        assertFalse(afterAutoArchiveRun!!.archived)
        assertFalse(afterAutoArchiveRun.autoArchive)
    }

    @Test
    fun nonExpiredPassDoesNotGetArchived() {
        val nonExpiredPass = Pass(
            id = "non-expired-pass",
            description = "Not yet expired pass",
            formatVersion = 1,
            organization = "Test Org",
            serialNumber = "125",
            type = PassType.Generic,
            barCodes = setOf(),
            addedAt = fixedNow,
            expirationDate = fixedFutureDate,
        )
        passDao.insert(nonExpiredPass)

        passRepository.archiveExpiredPasses(now = fixedNow)

        val afterAutoArchiveRun = passDao.findById(nonExpiredPass.id)?.pass
        assertNotNull(afterAutoArchiveRun)
        assertFalse(afterAutoArchiveRun!!.archived)
        assertTrue(afterAutoArchiveRun.autoArchive)
    }
}
