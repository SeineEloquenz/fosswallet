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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.ZonedDateTime

@RunWith(AndroidJUnit4::class)
class AutoArchiveBehaviorTest {

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
    fun unarchivedExpiredPassWithAutoArchiveDisabledDoesNotGetArchivedAgain() {
        val expiredPass = Pass(
            id = "expired-pass",
            description = "Expired pass",
            formatVersion = 1,
            organization = "Test Org",
            serialNumber = "123",
            type = PassType.Generic,
            barCodes = setOf(),
            addedAt = Instant.now(),
            expirationDate = ZonedDateTime.now().minusDays(1),
            archived = true,
            autoArchive = true,
        )
        passDao.insert(expiredPass)

        passDao.unarchive(expiredPass.id)
        val afterUnarchive = passDao.findById(expiredPass.id)?.pass
        assertNotNull(afterUnarchive)
        assertFalse(afterUnarchive!!.archived)
        assertFalse(afterUnarchive.autoArchive)

        passRepository.archiveExpiredPasses(now = Instant.now())

        val afterAutoArchiveRun = passDao.findById(expiredPass.id)?.pass
        assertNotNull(afterAutoArchiveRun)
        assertFalse(afterAutoArchiveRun!!.archived)
        assertFalse(afterAutoArchiveRun.autoArchive)
    }
}
