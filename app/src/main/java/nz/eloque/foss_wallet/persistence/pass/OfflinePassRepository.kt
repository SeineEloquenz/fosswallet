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
import nz.eloque.foss_wallet.persistence.PassBitmaps

class OfflinePassRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val passDao: PassDao
) : PassRepository {

    override fun all(): Flow<List<PassWithLocalization>> = passDao.all()

    override suspend fun updatable(): List<Pass> = passDao.updatable()

    override suspend fun filtered(query: String): Flow<List<PassWithLocalization>> {
        return if (query.isEmpty()) {
            all()
        } else {
            val result = all()
            result.map { it.filter { it.pass.contains(query) } } }
    }

    override suspend fun byId(id: String): PassWithLocalization = passDao.byId(id)

    override suspend fun associate(pass: Pass, group: PassGroup) = passDao.associate(pass.id, group.id)

    override suspend fun insert(pass: Pass, bitmaps: PassBitmaps, originalPass: OriginalPass) {
        val id = pass.id
        passDao.insert(pass)
        bitmaps.saveToDisk(context, id)
        originalPass.saveToDisk(context, id)
    }

    override suspend fun insert(group: PassGroup): PassGroup {
        val id = passDao.insert(group)
        return group.copy(id)
    }

    override suspend fun delete(pass: Pass) {
        pass.deleteFiles(context)
        passDao.delete(pass)
    }

    override suspend fun deleteGroup(groupId: Long) = passDao.delete(PassGroup(groupId))
}
