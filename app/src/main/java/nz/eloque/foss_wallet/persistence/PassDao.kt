package nz.eloque.foss_wallet.persistence

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import nz.eloque.foss_wallet.model.Pass

@Dao
interface PassDao {
    @Query("SELECT * FROM pass")
    fun all(): Flow<List<Pass>>

    @Query("SELECT * FROM pass WHERE id=:id")
    fun byId(id: Int): Pass

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(pass: Pass): Long

    @Delete
    fun delete(pass: Pass)
}
