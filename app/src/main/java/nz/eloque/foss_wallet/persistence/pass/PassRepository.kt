package nz.eloque.foss_wallet.persistence.pass

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nz.eloque.foss_wallet.model.OriginalPass
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassGroup
import nz.eloque.foss_wallet.model.PassWithLocalization
import nz.eloque.foss_wallet.persistence.loader.PassBitmaps

class PassRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val passDao: PassDao
) {

    fun all(authStatus: Boolean): Flow<List<PassWithLocalization>> = passDao.all(authStatus)

    fun updatable(): List<Pass> = passDao.updatable()

    fun filtered(query: String, authStatus: Boolean): Flow<List<PassWithLocalization>> {
        return if (query.isEmpty()) {
            all(authStatus)
        } else {
            val result = all(authStatus)
            result.map { it.filter { it.pass.contains(query) } } }
    }

    fun byId(id: String): PassWithLocalization = passDao.byId(id)

    fun findById(id: String): PassWithLocalization? = passDao.findById(id)

    fun associate(pass: Pass, group: PassGroup) = passDao.associate(pass.id, group.id)

    fun insert(pass: Pass, bitmaps: PassBitmaps, originalPass: OriginalPass) {
        val id = pass.id
        passDao.insert(pass)
        bitmaps.saveToDisk(context, id)
        originalPass.saveToDisk(context, id)
    }

    fun insert(group: PassGroup): PassGroup {
        val id = passDao.insert(group)
        return group.copy(id)
    }

    fun delete(pass: Pass) {
        pass.deleteFiles(context)
        passDao.delete(pass)
    }

    fun deleteGroup(groupId: Long) = passDao.delete(PassGroup(groupId))
    fun associate(groupId: Long, passes: Set<Pass>) = passDao.associate(groupId, passes)
    fun dissociate(pass: Pass, groupId: Long) = passDao.dissociate(pass, groupId)

    fun archive(pass: Pass) = passDao.archive(pass.id)
    fun unarchive(pass: Pass) = passDao.unarchive(pass.id)

    suspend fun hide(pass: Pass) = passDao.hide(pass.id)
    suspend fun unhide(pass: Pass) = passDao.unhide(pass.id)

    fun hidden(pass: Pass) = passDao.hidden(pass.id)

    suspend fun pin(pass: Pass) = passDao.pin(pass.id)
    suspend fun unpin(pass: Pass) = passDao.unpin(pass.id)

    fun pinned(pass: Pass) = passDao.pinned(pass.id)
}
