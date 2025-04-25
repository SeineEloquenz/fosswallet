package nz.eloque.foss_wallet.persistence.pass

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nz.eloque.foss_wallet.model.OriginalPass
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassWithLocalization
import nz.eloque.foss_wallet.persistence.PassBitmaps

class OfflinePassRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val passDao: PassDao
) : PassRepository {

    override fun all(): Flow<List<PassWithLocalization>> = passDao.all()

    override suspend fun filtered(query: String): Flow<List<PassWithLocalization>> {
        return if (query.isEmpty()) {
            all()
        } else {
            val result = all()
            result.map { it.filter { it.pass.contains(query) } } }
    }

    override suspend fun byId(id: Long): PassWithLocalization = passDao.byId(id)

    override suspend fun insert(pass: Pass, bitmaps: PassBitmaps, originalPass: OriginalPass): Long {
        val id = passDao.insert(pass)
        bitmaps.saveToDisk(context, id)
        originalPass.saveToDisk(context, id)
        return id
    }

    override suspend fun delete(pass: Pass) {
        pass.deleteFiles(context)
        passDao.delete(pass)
    }
}
