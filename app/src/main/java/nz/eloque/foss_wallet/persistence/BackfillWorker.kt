package nz.eloque.foss_wallet.persistence

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import nz.eloque.foss_wallet.parsing.PassParser

@HiltWorker
class BackfillWorker
    @AssistedInject
    constructor(
        @Assisted appContext: Context,
        @Assisted workerParams: WorkerParameters,
        private val passStore: PassStore,
        private val settingsStore: SettingsStore,
    ) : CoroutineWorker(appContext, workerParams) {
        override suspend fun doWork(): Result {
            passStore.backfill()
            settingsStore.setBackfilledParserVersion(PassParser.VERSION)
            return Result.success()
        }

        companion object {
            private const val NAME = "backfill"

            fun enqueueIfNeeded(
                workManager: WorkManager,
                settingsStore: SettingsStore,
            ) {
                if (settingsStore.backfilledParserVersion() < PassParser.VERSION) {
                    workManager.enqueueUniqueWork(NAME, ExistingWorkPolicy.KEEP, OneTimeWorkRequestBuilder<BackfillWorker>().build())
                }
            }
        }
    }
