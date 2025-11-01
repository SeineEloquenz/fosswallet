package nz.eloque.foss_wallet.persistence.pass

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassGroup
import nz.eloque.foss_wallet.model.PassWithLocalization

@Dao
interface PassDao {

    @Transaction
    @Query("SELECT * FROM pass")
    fun all(): Flow<List<PassWithLocalization>>

    @Transaction
    @Query("SELECT * FROM pass WHERE webServiceUrl != ''")
    fun updatable(): List<Pass>

    @Transaction
    @Query("SELECT * FROM pass WHERE id=:id")
    fun flowById(id: String): Flow<PassWithLocalization?>

    @Transaction
    @Query("SELECT * FROM pass WHERE id=:id")
    fun findById(id: String): PassWithLocalization?

    @Query("UPDATE pass SET groupId = :groupId WHERE id = :passId")
    fun associate(passId: String, groupId: Long)

    @Query("UPDATE pass SET groupId = NULL WHERE id = :passId")
    fun dissociate(passId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(pass: Pass)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(group: PassGroup): Long

    @Delete
    fun delete(pass: Pass)

    @Delete
    fun delete(group: PassGroup)

    @Transaction
    fun associate(groupId: Long, passes: Set<Pass>) {
        for (pass in passes) {
            associate(pass.id, groupId)
        }
    }

    @Transaction
    fun dissociate(pass: Pass, groupId: Long) {
        dissociate(pass.id)
        deleteEmptyGroup(groupId)
    }

    @Query("""
        DELETE FROM PassGroup
        WHERE id = :groupId 
        AND (
          SELECT COUNT(*) FROM Pass WHERE Pass.groupId = :groupId
        ) = 1
    """)
    fun deleteEmptyGroup(groupId: Long)

    @Query("UPDATE pass SET archived = 1 WHERE id = :passId")
    fun archive(passId: String)

    @Query("UPDATE pass SET archived = 0 WHERE id = :passId")
    fun unarchive(passId: String)

    @Query("UPDATE pass SET renderLegacy = :renderLegacy WHERE id = :passId")
    fun setLegacyRendering(passId: String, renderLegacy: Boolean)
}
