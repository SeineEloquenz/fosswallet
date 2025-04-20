package nz.eloque.foss_wallet.api

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import nz.eloque.foss_wallet.persistence.PassStore

@HiltWorker
class UpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val passStore: PassStore,
)
    : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val pass = passStore.passById(inputData.getString("id")!!.toLong())
        val result = passStore.update(pass.pass)
        return if (result != null) {
            Result.success()
        } else {
            Result.retry()
        }
        return Result.success()
    }
}