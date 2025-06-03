package nz.eloque.foss_wallet.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity
data class PassGroup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
)

data class GroupWithPasses(
    @Embedded val group: PassGroup,
    @Relation(
        parentColumn = "id",
        entityColumn = "groupId"
    )
    val passes: List<Pass>
)