package nz.eloque.foss_wallet.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Pass::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("passId"),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class Attachment(
    @PrimaryKey
    val fileName: String,
    val passId: String,
)
