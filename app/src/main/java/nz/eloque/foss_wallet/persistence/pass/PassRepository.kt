package nz.eloque.foss_wallet.persistence.pass

import kotlinx.coroutines.flow.Flow
import nz.eloque.foss_wallet.model.OriginalPass
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassGroup
import nz.eloque.foss_wallet.model.PassWithLocalization
import nz.eloque.foss_wallet.persistence.PassBitmaps

interface PassRepository {
    fun all(): Flow<List<PassWithLocalization>>

    suspend fun updatable(): List<Pass>

    suspend fun filtered(query: String) : Flow<List<PassWithLocalization>>

    suspend fun byId(id: String): PassWithLocalization

    suspend fun associate(pass: Pass, group: PassGroup)

    suspend fun insert(pass: Pass, bitmaps: PassBitmaps, originalPass: OriginalPass)

    suspend fun insert(group: PassGroup): PassGroup

    suspend fun delete(pass: Pass)
    suspend fun deleteGroup(groupId: Long)
    fun associate(groupId: Long, passes: Set<Pass>)
}