package nz.eloque.foss_wallet.persistence

import android.content.Context
import kotlinx.coroutines.flow.Flow
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassWithLocalization

class OfflinePassRepository(
    private val context: Context,
    private val passDao: PassDao
) : PassRepository {

    override fun all(): Flow<List<PassWithLocalization>> = passDao.all()

    override suspend fun byId(id: Int): PassWithLocalization = passDao.byId(id)

    override suspend fun insert(data: Pair<Pass, PassBitmaps>): Long {
        val (pass, bitmaps) = data
        val id = passDao.insert(pass)
        bitmaps.saveToDisk(context, id)
        return id
    }

    override suspend fun delete(pass: Pass) {
        pass.deleteFiles(context)
        passDao.delete(pass)
    }
}
