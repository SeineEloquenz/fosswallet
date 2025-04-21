package nz.eloque.foss_wallet.persistence.pass

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassWithLocalization

@Dao
interface PassDao {
    @Transaction
    @Query("SELECT * FROM pass")
    fun all(): Flow<List<PassWithLocalization>>

    @Transaction
    @Query("SELECT * FROM pass WHERE id=:id")
    fun byId(id: Long): PassWithLocalization

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(pass: Pass): Long

    @Delete
    fun delete(pass: Pass)
}
