package nz.eloque.foss_wallet.model

import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.Relation

@Entity
data class PassGroup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
)

data class GroupWithPasses(
    @Embedded val group: PassGroup,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["groupId"],
    )
    val passes: List<Pass>,
)
