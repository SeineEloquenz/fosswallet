package nz.eloque.foss_wallet.api

import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import jakarta.inject.Inject
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.persistence.SettingsStore
import nz.eloque.foss_wallet.persistence.pass.PassRepository
import java.util.concurrent.TimeUnit
import kotlin.time.toJavaDuration

class UpdateScheduler @Inject constructor(
    private val passRepository: PassRepository,
    private val settingsStore: SettingsStore,
    private val workManager: WorkManager
) {
    fun disableSync() {
        val updatablePasses = passRepository.updatable()
        updatablePasses.forEach { cancelUpdate(it) }
    }

    fun enableSync() {
        val updatablePasses = passRepository.updatable()
        updatablePasses.forEach { scheduleUpdate(it) }
    }

    fun updateSyncInterval() {
        val updatablePasses = passRepository.updatable()
        updatablePasses.forEach { cancelUpdate(it) }
        updatablePasses.forEach { scheduleUpdate(it) }
    }

    fun scheduleUpdate(pass: Pass) {
        if (settingsStore.isSyncEnabled()) {
            Log.i(TAG, "Scheduled update for pass ${pass.id}")
            val workRequest = PeriodicWorkRequestBuilder<UpdateWorker>(settingsStore.syncInterval().toJavaDuration())
                .setInputData(Data.Builder().putString("id", pass.id).build())
                .addTag("update")
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                .build()
            workManager.enqueueUniquePeriodicWork(
                pass.id,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        }
    }

    fun cancelUpdate(pass: Pass) {
        Log.i(TAG, "Canceled update for pass ${pass.id}")
        workManager.cancelUniqueWork(pass.id)
    }

    companion object {
        const val TAG = "UpdateScheduler"
    }
}