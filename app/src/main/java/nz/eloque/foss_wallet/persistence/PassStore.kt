package nz.eloque.foss_wallet.persistence

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import nz.eloque.foss_wallet.api.ImportResult
import nz.eloque.foss_wallet.api.PassbookApi
import nz.eloque.foss_wallet.api.UpdateContent
import nz.eloque.foss_wallet.api.UpdateResult
import nz.eloque.foss_wallet.api.UpdateScheduler
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassGroup
import nz.eloque.foss_wallet.notifications.NotificationService
import nz.eloque.foss_wallet.parsing.PassParser
import nz.eloque.foss_wallet.persistence.loader.PassLoadResult
import nz.eloque.foss_wallet.persistence.loader.PassLoader
import nz.eloque.foss_wallet.persistence.localization.PassLocalizationRepository
import nz.eloque.foss_wallet.persistence.pass.PassRepository
import nz.eloque.foss_wallet.shortcut.Shortcut
import java.util.Locale

class PassStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val notificationService: NotificationService,
    private val passRepository: PassRepository,
    private val localizationRepository: PassLocalizationRepository,
    private val updateScheduler: UpdateScheduler,
) {

    fun allPasses() = passRepository.all()

    fun passById(id: String) = passRepository.byId(id)

    fun filtered(query: String) = passRepository.filtered(query)

    fun add(loadResult: PassLoadResult): ImportResult {
        val existing = passRepository.findById(loadResult.pass.pass.id)
        val result = if (existing != null) ImportResult.Replaced else ImportResult.New
        
        insert(loadResult)
        if (loadResult.pass.pass.updatable()) {
            updateScheduler.scheduleUpdate(loadResult.pass.pass)
        }
        
        return result
    }

    suspend fun update(pass: Pass): UpdateResult {
        val updated = PassbookApi.getUpdated(pass)
        return if (updated is UpdateResult.Success && updated.content is UpdateContent.LoadResult) {
            insert(updated.content.result)
            notificationService.createNotificationChannel()
            val localizedPass = updated.content.result.pass.applyLocalization(Locale.getDefault().language)
            localizedPass.updatedFields(pass).forEach { notificationService.post(it.changeMessage) }
            UpdateResult.Success(UpdateContent.Pass(localizedPass))
        } else {
            updated
        }
    }

    fun archive(pass: Pass) = passRepository.archive(pass)

    fun unarchive(pass: Pass) = passRepository.unarchive(pass)

    fun group(passes: Set<Pass>): PassGroup {
        val group = passRepository.insert(PassGroup())
        passes.forEach { passRepository.associate(it, group) }
        return group
    }

    fun delete(pass: Pass) {
        passRepository.delete(pass)
        updateScheduler.cancelUpdate(pass)
        Shortcut.remove(context, pass)
    }

    fun load(context: Context, bytes: ByteArray): ImportResult {
        val loaded = PassLoader(PassParser(context)).load(bytes)
        return add(loaded)
    }

    private fun insert(loadResult: PassLoadResult) {
        val passWithLocalization = loadResult.pass
        passRepository.insert(passWithLocalization.pass, loadResult.bitmaps, loadResult.originalPass)
        passWithLocalization.localizations.map { it.copy(passId = passWithLocalization.pass.id) }.forEach { localizationRepository.insert(it) }
    }

    fun deleteGroup(groupId: Long) = passRepository.deleteGroup(groupId)
    fun associate(groupId: Long, passes: Set<Pass>) = passRepository.associate(groupId, passes)
    fun dessociate(pass: Pass, groupId: Long) = passRepository.dessociate(pass, groupId)

    fun unhiddenPasses() = passRepository.unhidden()
    fun filteredUnhidden(query: String) = passRepository.filteredUnhidden(query)
    fun hide(pass: Pass) = passRepository.hide(pass)
    fun unhide(pass: Pass) = passRepository.unhide(pass)
}
