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
import nz.eloque.foss_wallet.model.PassTagCrossRef
import nz.eloque.foss_wallet.model.PassWithTagsAndLocalization
import nz.eloque.foss_wallet.model.Tag
import nz.eloque.foss_wallet.persistence.loader.PassBitmaps
import java.time.Instant
import java.util.Locale

class PassRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val passDao: PassDao
) {

    fun all(): Flow<List<PassWithTagsAndLocalization>> = passDao.all()

    fun updatable(): List<Pass> = passDao.updatable()

    fun filtered(query: String): Flow<List<PassWithTagsAndLocalization>> {
        return if (query.isEmpty()) {
            all()
        } else {
            val result = all()
            result.map { passes -> passes.filter { it.pass.contains(query) } } }
    }

    fun flowById(id: String): Flow<LocalizedPassWithTags?> = passDao.flowById(id).map { it?.applyLocalization(Locale.getDefault().language) }

    fun findById(id: String): LocalizedPassWithTags? = passDao.findById(id)?.applyLocalization(Locale.getDefault().language)

    fun associate(pass: Pass, group: PassGroup) = passDao.associate(pass.id, group.id)

    suspend fun tag(pass: Pass, tag: Tag) = passDao.tag(PassTagCrossRef(pass.id, tag.label))

    suspend fun untag(pass: Pass, tag: Tag) = passDao.untag(PassTagCrossRef(pass.id, tag.label))

    fun insert(pass: Pass, bitmaps: PassBitmaps, originalPass: OriginalPass?) {
        val id = pass.id
        passDao.insert(pass)
        bitmaps.saveToDisk(context, id)
        originalPass?.saveToDisk(context, id)
    }

    fun insert(group: PassGroup): PassGroup {
        val id = passDao.insert(group)
        return group.copy(id = id)
    }

    fun delete(pass: Pass) {
        pass.deleteFiles(context)
        passDao.delete(pass)
    }

    fun dissociate(pass: Pass, groupId: Long) = passDao.dissociate(pass, groupId)

    fun deleteGroup(groupId: Long) = passDao.delete(PassGroup(groupId))
    fun associate(groupId: Long, passes: Set<Pass>) = passDao.associate(groupId, passes)
    fun archive(pass: Pass) = passDao.archive(pass.id)
    fun unarchive(pass: Pass) = passDao.unarchive(pass.id)
    fun toggleLegacyRendering(pass: Pass) = passDao.setLegacyRendering(pass.id, !pass.renderLegacy)

    fun archiveExpiredPasses(now: Instant = Instant.now()) {
        passDao.nonArchivedWithExpirationDate()
            .filter { pass ->
                pass.expirationDate?.toInstant()?.let { expiration -> !expiration.isAfter(now) } ?: false
            }
            .forEach { passDao.archive(it.id) }
    }
}
