package nz.eloque.foss_wallet.persistence.localization

import kotlinx.coroutines.flow.Flow
import nz.eloque.foss_wallet.model.PassLocalization

interface PassLocalizationRepository {
    fun all(): Flow<List<PassLocalization>>

    suspend fun byPassId(passId: Int, lang: String): Set<PassLocalization>

    suspend fun insert(localization: PassLocalization): Long
}