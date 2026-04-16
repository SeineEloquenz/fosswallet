package nz.eloque.foss_wallet.persistence.pass

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassGroup
import nz.eloque.foss_wallet.model.PassMetadata
import nz.eloque.foss_wallet.model.PassTagCrossRef
import nz.eloque.foss_wallet.model.PassWithMetadata

@Dao
interface PassDao {
    @Transaction
    @Query("SELECT * FROM pass")
    fun all(): Flow<List<PassWithMetadata>>

    @Transaction
    @Query("SELECT * FROM pass WHERE webServiceUrl != ''")
    fun updatable(): List<Pass>

    @Transaction
    @Query("SELECT * FROM pass WHERE id=:id")
    fun flowById(id: String): Flow<PassWithMetadata?>

    @Transaction
    @Query("SELECT * FROM pass WHERE id=:id")
    fun findById(id: String): PassWithMetadata?

    @Query(
        """
        UPDATE PassMetadata
        SET groupId = :groupId
        WHERE passId = :passId
    """,
    )
    suspend fun associate(
        passId: String,
        groupId: Long,
    )

    @Query(
        """
        UPDATE PassMetadata
        SET groupId = NULL
        WHERE passId = :passId
    """,
    )
    suspend fun dissociate(passId: String)

    @Upsert
    fun insert(pass: Pass)

    @Upsert
    fun insert(passMetadata: PassMetadata)

    @Upsert
    fun insert(group: PassGroup): Long

    @Delete
    suspend fun delete(pass: Pass)

    @Delete
    suspend fun delete(group: PassGroup)

    @Transaction
    suspend fun associate(
        groupId: Long,
        passes: Set<Pass>,
    ) {
        for (pass in passes) {
            associate(pass.id, groupId)
        }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun tag(crossRef: PassTagCrossRef)

    @Delete
    suspend fun untag(crossRef: PassTagCrossRef)

    @Transaction
    suspend fun dissociate(
        pass: Pass,
        groupId: Long,
    ) {
        dissociate(pass.id)
        deleteEmptyGroup(groupId)
    }

    @Transaction
    @Query("SELECT * FROM PassMetadata WHERE passId = :passId")
    suspend fun metadata(passId: String): PassMetadata?

    @Query(
        """
        DELETE FROM PassGroup
        WHERE id = :groupId
        AND (
            SELECT COUNT(*)
            FROM PassMetadata
            WHERE groupId = :groupId
        ) = 1
    """,
    )
    suspend fun deleteEmptyGroup(groupId: Long)

    @Query("UPDATE PassMetadata SET archived = 1 WHERE passId = :passId")
    suspend fun archive(passId: String)

    @Query("UPDATE PassMetadata SET archived = 0, autoArchive = 0 WHERE passId = :passId")
    suspend fun unarchive(passId: String)

    @Query(
        """
        UPDATE PassMetadata
        SET renderLegacy = NOT renderLegacy
        WHERE passId = :passId
    """,
    )
    suspend fun toggleLegacyRendering(passId: String)

    @Query(
        """
        SELECT p.*
        FROM Pass p
        INNER JOIN PassMetadata m
            ON p.id = m.passId
        WHERE m.archived = 0
          AND m.autoArchive = 1
          AND p.expirationDate IS NOT NULL
    """,
    )
    suspend fun nonArchivedWithExpirationDate(): List<Pass>
}
