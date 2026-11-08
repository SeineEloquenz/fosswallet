package nz.eloque.foss_wallet.model

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import java.time.Instant
import java.util.UUID

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
    val updatedAt: Instant? = null,
    val registeredDeviceId: UUID? = null,
)
