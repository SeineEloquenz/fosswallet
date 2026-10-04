package nz.eloque.foss_wallet.persistence

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.map
import nz.eloque.foss_wallet.api.FailureReason
import nz.eloque.foss_wallet.api.ImportOutcome
import nz.eloque.foss_wallet.api.ImportResult
import nz.eloque.foss_wallet.api.PassbookApi
import nz.eloque.foss_wallet.api.UpdateContent
import nz.eloque.foss_wallet.api.UpdateResult
import nz.eloque.foss_wallet.api.UpdateScheduler
import nz.eloque.foss_wallet.model.Attachment
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassGroup
import nz.eloque.foss_wallet.model.Tag
import nz.eloque.foss_wallet.notifications.NotificationService
import nz.eloque.foss_wallet.parsing.PassParser
import nz.eloque.foss_wallet.persistence.loader.InvalidInputException
import nz.eloque.foss_wallet.persistence.loader.InvalidPassException
import nz.eloque.foss_wallet.persistence.loader.Loader
import nz.eloque.foss_wallet.persistence.loader.PassBitmaps
import nz.eloque.foss_wallet.persistence.loader.PassLoadResult
import nz.eloque.foss_wallet.persistence.loader.PassLoader
import nz.eloque.foss_wallet.persistence.localization.PassLocalizationRepository
import nz.eloque.foss_wallet.persistence.pass.PassRepository
import nz.eloque.foss_wallet.shortcut.ShortcutService
import java.util.Locale

private const val TAG = "PassStore"

class PassStore
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        private val transactionalExecutor: TransactionalExecutor,
        private val notificationService: NotificationService,
        private val passRepository: PassRepository,
        private val localizationRepository: PassLocalizationRepository,
        private val updateScheduler: UpdateScheduler,
        private val shortcutService: ShortcutService,
        private val passbookApi: PassbookApi,
    ) {
        fun allPasses() = passRepository.all().map { passes -> passes.map { it.applyLocalization(Locale.getDefault().language) } }

        fun passById(id: String) = passRepository.findById(id)

        fun passFlowById(id: String) = passRepository.flowById(id)

        fun passesInGroup(groupId: Long) = passRepository.flowByGroup(groupId)

        fun filtered(query: String) =
            passRepository.filtered(query).map { passes ->
                passes.map {
                    it.applyLocalization(Locale.getDefault().language)
                }
            }

        suspend fun create(
            pass: Pass,
            bitmaps: PassBitmaps,
        ) {
            passRepository.insert(pass, bitmaps, null)
        }

        suspend fun add(loadResult: PassLoadResult): ImportResult {
            val pass = loadResult.pass.pass
            val existing = passRepository.findById(pass.id)

            insert(loadResult)
            if (pass.updatable()) updateScheduler.scheduleUpdate(pass)
            if (existing != null) return ImportResult.Replaced
            if (passRepository.metadata(pass.id)?.archived == true) {
                passRepository.archive(pass)
                return ImportResult.AutoArchived
            }
            return ImportResult.New
        }

        suspend fun update(pass: Pass): UpdateResult {
            val service = pass.webService() ?: return UpdateResult.NotUpdated
            val updated =
                passbookApi.getUpdated(
                    service = service,
                    registered = passRepository.isRegistered(pass),
                    onRegistered = { passRepository.setRegistered(pass) },
                )
            if (updated !is UpdateResult.Success || updated.content !is UpdateContent.Downloaded) return updated
            val loadResult =
                try {
                    PassLoader(PassParser()).load(updated.content.bytes, pass.id, pass.addedAt, pass.deviceId)
                } catch (e: InvalidPassException) {
                    return UpdateResult.Failed(FailureReason.Exception(e))
                }
            insert(loadResult)
            passRepository.setUpdatedAt(pass)
            notificationService.createNotificationChannel()
            val localizedPass =
                loadResult.pass
                    .applyLocalization(Locale.getDefault().language)
            localizedPass.updatedFields(pass).forEach { notificationService.post(it.changeMessage) }
            return UpdateResult.Success(UpdateContent.Pass(localizedPass))
        }

        suspend fun archive(pass: Pass) = passRepository.archive(pass)

        suspend fun unarchive(pass: Pass) = passRepository.unarchive(pass)

        suspend fun tag(
            pass: Pass,
            tag: Tag,
        ) = passRepository.tag(pass, tag)

        suspend fun untag(
            pass: Pass,
            tag: Tag,
        ) = passRepository.untag(pass, tag)

        suspend fun toggleLegacyRendering(pass: Pass) = passRepository.toggleLegacyRendering(pass)

        suspend fun group(passes: Set<Pass>): PassGroup {
            val group = passRepository.insert(PassGroup())
            passes.forEach { passRepository.associate(it, group) }
            return group
        }

        suspend fun delete(pass: Pass) {
            val registered = passRepository.isRegistered(pass)
            passRepository.delete(pass)
            updateScheduler.cancelUpdate(pass)
            shortcutService.disable(pass)
            if (registered) pass.webService()?.let { passbookApi.unregister(it) }
        }

        suspend fun delete(attachment: Attachment) {
            passRepository.delete(attachment)
        }

        suspend fun import(bytes: ByteArray): ImportOutcome {
            val loaded =
                try {
                    Loader(context).load(bytes)
                } catch (e: InvalidInputException) {
                    Log.w(TAG, "Failed to import file", e)
                    return ImportOutcome.Invalid
                }
            return when (loaded.size) {
                0 -> {
                    ImportOutcome.Empty
                }

                1 -> {
                    val single = loaded.first()
                    ImportOutcome.Single(single.pass.pass.id, add(single))
                }

                else -> {
                    loaded.forEach { add(it) }
                    group(loaded.map { it.pass.pass }.toSet())
                    ImportOutcome.Multiple(loaded.size)
                }
            }
        }

        private suspend fun insert(loadResult: PassLoadResult) {
            transactionalExecutor.runTransactionally {
                val passWithLocalization = loadResult.pass
                passRepository.insert(passWithLocalization.pass, loadResult.bitmaps, loadResult.originalPass)
                passWithLocalization.localizations
                    .map {
                        it.copy(
                            passId = passWithLocalization.pass.id,
                        )
                    }.forEach { localizationRepository.insert(it) }
            }
        }

        suspend fun archiveExpiredPasses() = passRepository.archiveExpiredPasses()

        suspend fun deleteGroup(groupId: Long) = passRepository.deleteGroup(groupId)

        suspend fun associate(
            groupId: Long,
            passes: Set<Pass>,
        ) = passRepository.associate(groupId, passes)

        suspend fun dissociate(
            pass: Pass,
            groupId: Long,
        ) = passRepository.dissociate(pass, groupId)

        suspend fun attach(
            pass: Pass,
            name: String,
            bytes: ByteArray,
        ) = passRepository.insertAttachment(pass, name, bytes)
    }
