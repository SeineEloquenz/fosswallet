package nz.eloque.foss_wallet.persistence

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import nz.eloque.foss_wallet.parsing.PassParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockingDetails
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackfillWorkerTest {
    private val settingsStore =
        SettingsStore(ApplicationProvider.getApplicationContext<Context>().getSharedPreferences("test", Context.MODE_PRIVATE))
    private val workManager = mock(WorkManager::class.java)

    private fun enqueued() = mockingDetails(workManager).invocations.filter { it.method.name == "enqueueUniqueWork" }

    @Test
    fun `enqueues a backfill when passes were parsed by an older parser`() {
        settingsStore.setBackfilledParserVersion(PassParser.VERSION - 1)

        BackfillWorker.enqueueIfNeeded(workManager, settingsStore)

        assertEquals(ExistingWorkPolicy.KEEP, enqueued().single().arguments[1])
    }

    @Test
    fun `does nothing once passes are backfilled`() {
        settingsStore.setBackfilledParserVersion(PassParser.VERSION)

        BackfillWorker.enqueueIfNeeded(workManager, settingsStore)

        assertTrue(enqueued().isEmpty())
    }
}
