package nz.eloque.foss_wallet.model

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "localization",
    primaryKeys = ["passId", "lang", "label"],
    foreignKeys = [ForeignKey(
        entity = Pass::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("passId"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class PassLocalization(
    val passId: Long,
    val lang: String,
    val label: String,
    val text: String,
)