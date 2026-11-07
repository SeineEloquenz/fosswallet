package nz.eloque.foss_wallet.contentprovider

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassMetadata
import nz.eloque.foss_wallet.model.PassTagCrossRef
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.model.Tag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CatimaContentProviderTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val entryPoint = EntryPointAccessors.fromApplication(context, ProviderEntrypoint::class.java)
    private val db = entryPoint.walletDb()

    @Before
    fun setUp() {
        Robolectric.setupContentProvider(CatimaContentProvider::class.java, CatimaContentProvider.AUTHORITY)
    }

    @Test
    fun reportsMinorVersionTwo() {
        context.contentResolver.query(Uri.parse("content://${CatimaContentProvider.AUTHORITY}/version"), null, null, null, null)!!.use {
            it.moveToFirst()
            assertEquals(2, it.getInt(it.getColumnIndexOrThrow(CatimaContentProvider.Version.MINOR_COLUMN)))
        }
    }

    @Test
    fun notifiesEachUriOnChange() {
        val pass =
            Pass(
                id = "pass",
                description = "Pass",
                formatVersion = 1,
                organization = "Test Org",
                serialNumber = "123",
                type = PassType.Generic,
                barCodes = setOf(),
                addedAt = Instant.EPOCH,
            )
        runBlocking(Dispatchers.IO) {
            db.passDao().insert(pass)
            db.passDao().insert(PassMetadata(pass.id))
            db.tagDao().insert(Tag("tag", Color.Red))
            db.passDao().tag(PassTagCrossRef(pass.id, "tag"))
        }

        val expected =
            setOf(CatimaContentProvider.CARDS_URI, CatimaContentProvider.GROUPS_URI, CatimaContentProvider.CARD_GROUPS_URI)
        assertTrue(awaitNotified(expected))
    }

    private fun awaitNotified(expected: Set<Uri>): Boolean {
        val resolver = shadowOf(context.contentResolver)
        repeat(50) {
            if (resolver.notifiedUris.map { it.uri }.containsAll(expected)) return true
            Thread.sleep(100)
        }
        return false
    }
}
