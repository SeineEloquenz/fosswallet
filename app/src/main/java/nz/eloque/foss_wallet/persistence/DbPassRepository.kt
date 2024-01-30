package nz.eloque.foss_wallet.persistence

import kotlinx.coroutines.flow.Flow
import nz.eloque.foss_wallet.model.Pass

class DbPassRepository(private val passDao: PassDao) : PassRepository {

    override fun all(): Flow<List<Pass>> = passDao.all()

    override suspend fun byId(id: Int): Pass = passDao.byId(id)

    override suspend fun insertAll(vararg pass: Pass) = passDao.insertAll(*pass)

    override suspend fun delete(pass: Pass) = passDao.delete(pass)
}
