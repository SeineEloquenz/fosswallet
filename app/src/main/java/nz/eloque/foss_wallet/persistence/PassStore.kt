package nz.eloque.foss_wallet.persistence

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import jakarta.inject.Inject
import nz.eloque.foss_wallet.api.PassbookApi
import nz.eloque.foss_wallet.api.UpdateWorker
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassLocalization
import nz.eloque.foss_wallet.parsing.PassParser
import nz.eloque.foss_wallet.persistence.localization.PassLocalizationRepository
import nz.eloque.foss_wallet.persistence.pass.PassRepository
import java.io.InputStream
import java.util.Locale
import java.util.concurrent.TimeUnit

class PassStore @Inject constructor(
    private val passRepository: PassRepository,
    private val localizationRepository: PassLocalizationRepository,
    private val workManager: WorkManager
) {

    fun allPasses() = passRepository.all()

    suspend fun passById(id: Long) = passRepository.byId(id)

    suspend fun add(pass: Pass, bitmaps: PassBitmaps, localization: Set<PassLocalization>): Long {
        val id = insert(pass, bitmaps, localization)
        scheduleUpdate(pass)
        return id
    }

    suspend fun update(pass: Pass): Pass? {
        val updated = PassbookApi.getUpdated(pass)
        return if (updated != null) {
            val id = insert(updated.first, updated.second, updated.third)
            passById(id).applyLocalization(Locale.getDefault().language)
        } else {
            null
        }
    }

    suspend fun delete(pass: Pass) {
        passRepository.delete(pass)
        cancelUpdate(pass)
    }

    suspend fun load(context: Context, inputStream: InputStream) {
        val (pass, bitmaps, localizations) = PassLoader(PassParser(context)).load(inputStream)
        add(pass, bitmaps, localizations)
    }

    private suspend fun insert(pass: Pass, bitmaps: PassBitmaps, localization: Set<PassLocalization>): Long {
        val id = passRepository.insert(Pair(pass, bitmaps))
        localization.map { it.copy(passId = id) }.forEach { localizationRepository.insert(it) }
        return id
    }

    private fun scheduleUpdate(pass: Pass) {
        val workRequest = PeriodicWorkRequestBuilder<UpdateWorker>(15, TimeUnit.MINUTES)
            .setInputData(Data.Builder().putString("id", pass.id.toString()).build())
            .addTag("update")
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()
        workManager.enqueueUniquePeriodicWork(
            pass.id.toString(),
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    private fun cancelUpdate(pass: Pass) {
        workManager.cancelUniqueWork(pass.id.toString())
    }
}