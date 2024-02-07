package nz.eloque.foss_wallet.persistence

import kotlinx.coroutines.flow.Flow
import nz.eloque.foss_wallet.model.Pass

interface PassRepository {
    fun all(): Flow<List<Pass>>

    suspend fun byId(id: Int): Pass

    suspend fun insert(data: Pair<Pass, PassBitmaps>): Long
    suspend fun delete(pass: Pass)
}