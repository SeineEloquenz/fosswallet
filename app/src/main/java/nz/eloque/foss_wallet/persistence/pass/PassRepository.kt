package nz.eloque.foss_wallet.persistence.pass

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nz.eloque.foss_wallet.model.LocalizedPassWithTags
import nz.eloque.foss_wallet.model.OriginalPass
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassGroup
import nz.eloque.foss_wallet.model.PassMetadata
import nz.eloque.foss_wallet.model.PassTagCrossRef
import nz.eloque.foss_wallet.model.PassWithMetadata
import nz.eloque.foss_wallet.model.Tag
import nz.eloque.foss_wallet.persistence.loader.PassBitmaps
import java.time.Instant
import java.util.Locale

class PassRepository
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        private val passDao: PassDao,
    ) {
        fun all(): Flow<List<PassWithMetadata>> = passDao.all()

        fun updatable(): List<Pass> = passDao.updatable()

        fun filtered(query: String): Flow<List<PassWithMetadata>> =
            if (query.isEmpty()) {
                all()
            } else {
                val result = all()
                result.map { passes -> passes.filter { it.pass.contains(query) } }
            }

        fun flowById(id: String): Flow<LocalizedPassWithTags?> =
            passDao.flowById(id).map { it?.applyLocalization(Locale.getDefault().language) }

        fun findById(id: String): LocalizedPassWithTags? = passDao.findById(id)?.applyLocalization(Locale.getDefault().language)

        suspend fun associate(
            pass: Pass,
            group: PassGroup,
        ) = passDao.associate(pass.id, group.id)

        suspend fun tag(
            pass: Pass,
            tag: Tag,
        ) = passDao.tag(PassTagCrossRef(pass.id, tag.label))

        suspend fun untag(
            pass: Pass,
            tag: Tag,
        ) = passDao.untag(PassTagCrossRef(pass.id, tag.label))

        suspend fun insert(
            pass: Pass,
            bitmaps: PassBitmaps,
            originalPass: OriginalPass?,
        ) {
            val id = pass.id
            passDao.insert(pass)
            var passMetadata = passDao.metadata(id) ?: PassMetadata(pass.id)
            passMetadata = passMetadata.copy(archived = shouldBeAutoArchived(pass, passMetadata))
            passDao.insert(passMetadata)
            bitmaps.saveToDisk(context, id)
            originalPass?.saveToDisk(context, id)
        }

        private fun shouldBeAutoArchived(
            pass: Pass,
            metadata: PassMetadata,
            now: Instant = Instant.now(),
        ): Boolean {
            val expired = pass.expirationDate?.toInstant()?.let { expiration -> !expiration.isAfter(now) } ?: false
            return metadata.autoArchive && expired
        }

        fun insert(group: PassGroup): PassGroup {
            val id = passDao.insert(group)
            return group.copy(id = id)
        }

        suspend fun delete(pass: Pass) {
            pass.deleteFiles(context)
            passDao.delete(pass)
        }

        suspend fun dissociate(
            pass: Pass,
            groupId: Long,
        ) = passDao.dissociate(pass, groupId)

        suspend fun deleteGroup(groupId: Long) = passDao.delete(PassGroup(groupId))

        suspend fun associate(
            groupId: Long,
            passes: Set<Pass>,
        ) = passDao.associate(groupId, passes)

        suspend fun archive(pass: Pass) = passDao.archive(pass.id)

        suspend fun unarchive(pass: Pass) = passDao.unarchive(pass.id)

        suspend fun toggleLegacyRendering(pass: Pass) = passDao.toggleLegacyRendering(pass.id)

        suspend fun archiveExpiredPasses(now: Instant = Instant.now()) {
            passDao
                .nonArchivedWithExpirationDate()
                .filter { pass ->
                    pass.expirationDate?.toInstant()?.let { expiration -> !expiration.isAfter(now) } ?: false
                }.forEach { passDao.archive(it.id) }
        }
    }
