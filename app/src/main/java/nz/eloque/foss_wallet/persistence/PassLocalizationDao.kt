package nz.eloque.foss_wallet.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import nz.eloque.foss_wallet.model.PassLocalization

@Dao
interface PassLocalizationDao {
    @Query("SELECT * FROM localization")
    fun all(): Flow<List<PassLocalization>>

    @Query("SELECT * FROM localization WHERE passId=:passId AND lang=:lang")
    fun byPassId(passId: Int, lang: String): List<PassLocalization>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(localization: PassLocalization): Long
}
