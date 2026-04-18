package nz.eloque.foss_wallet.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "PassMetadata",
    foreignKeys = [
        ForeignKey(
            entity = Pass::class,
            parentColumns = ["id"],
            childColumns = ["passId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = PassGroup::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["passId"]),
        Index(value = ["groupId"]),
    ],
)
data class PassMetadata(
    @PrimaryKey val passId: String,
    val groupId: Long? = null,
    val archived: Boolean = false,
    val autoArchive: Boolean = true,
    val renderLegacy: Boolean = false,
)
