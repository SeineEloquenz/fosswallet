package nz.eloque.foss_wallet.persistence.pass

import kotlinx.coroutines.flow.Flow
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassWithLocalization
import nz.eloque.foss_wallet.persistence.PassBitmaps

interface PassRepository {
    fun all(): Flow<List<PassWithLocalization>>

    suspend fun filtered(query: String) : Flow<List<PassWithLocalization>>

    suspend fun byId(id: Long): PassWithLocalization

    suspend fun insert(data: Pair<Pass, PassBitmaps>): Long

    suspend fun delete(pass: Pass)
}