package nz.eloque.foss_wallet.persistence.localization

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import nz.eloque.foss_wallet.model.PassLocalization

@Dao
interface PassLocalizationDao {
    @Query("SELECT * FROM localization")
    fun all(): Flow<List<PassLocalization>>

    @Query("SELECT * FROM localization WHERE passId=:passId AND lang=:lang")
    suspend fun byPassId(
        passId: Int,
        lang: String,
    ): List<PassLocalization>

    @Upsert
    suspend fun insert(localization: PassLocalization): Long
}
