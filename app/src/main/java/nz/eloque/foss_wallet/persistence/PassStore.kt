package nz.eloque.foss_wallet.persistence

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import nz.eloque.foss_wallet.api.PassbookApi
import nz.eloque.foss_wallet.api.UpdateContent
import nz.eloque.foss_wallet.api.UpdateResult
import nz.eloque.foss_wallet.api.UpdateScheduler
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassGroup
import nz.eloque.foss_wallet.notifications.NotificationService
import nz.eloque.foss_wallet.parsing.PassParser
import nz.eloque.foss_wallet.persistence.localization.PassLocalizationRepository
import nz.eloque.foss_wallet.persistence.pass.PassRepository
import nz.eloque.foss_wallet.shortcut.Shortcut
import java.io.InputStream
import java.util.Locale

class PassStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationService: NotificationService,
    private val passRepository: PassRepository,
    private val localizationRepository: PassLocalizationRepository,
    private val updateScheduler: UpdateScheduler,
) {

    fun allPasses() = passRepository.all()

    suspend fun passById(id: String) = passRepository.byId(id)

    suspend fun filtered(query: String) = passRepository.filtered(query)

    suspend fun add(loadResult: PassLoadResult) {
        insert(loadResult)
        if (loadResult.pass.updatable()) {
            updateScheduler.scheduleUpdate(loadResult.pass)
        }
    }

    suspend fun update(pass: Pass): UpdateResult {
        val updated = PassbookApi.getUpdated(pass)
        return if (updated is UpdateResult.Success && updated.content is UpdateContent.LoadResult) {
            insert(updated.content.result)
            notificationService.createNotificationChannel()
            updated.content.result.pass.updatedFields(pass).forEach { notificationService.post(it.changeMessage()) }
            UpdateResult.Success(UpdateContent.Pass(passById(updated.content.result.pass.id).applyLocalization(Locale.getDefault().language)))
        } else {
            updated
        }
    }

    suspend fun group(passes: Set<Pass>): PassGroup {
        val group = passRepository.insert(PassGroup())
        passes.forEach { passRepository.associate(it, group) }
        return group
    }

    suspend fun delete(pass: Pass) {
        passRepository.delete(pass)
        updateScheduler.cancelUpdate(pass)
        Shortcut.remove(context, pass)
    }

    suspend fun load(context: Context, inputStream: InputStream) {
        val loaded = PassLoader(PassParser(context)).load(inputStream)
        add(loaded)
    }

    private suspend fun insert(loadResult: PassLoadResult) {
        passRepository.insert(loadResult.pass, loadResult.bitmaps, loadResult.originalPass)
        loadResult.localizations.map { it.copy(passId = loadResult.pass.id) }.forEach { localizationRepository.insert(it) }
    }

    suspend fun deleteGroup(groupId: Long) = passRepository.deleteGroup(groupId)
}