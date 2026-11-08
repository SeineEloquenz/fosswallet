package nz.eloque.foss_wallet.persistence.tag

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow
import nz.eloque.foss_wallet.model.PassTagCrossRef
import nz.eloque.foss_wallet.model.Tag

@Dao
interface TagDao {
    @Transaction
    @Query("SELECT * FROM tag")
    fun all(): Flow<List<Tag>>

    @Transaction
    @Query("SELECT * FROM PassTag")
    suspend fun crossRef(): List<PassTagCrossRef>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: Tag)

    @Delete
    suspend fun remove(tag: Tag)
}
