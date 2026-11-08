package nz.eloque.foss_wallet.model

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index

@Entity(
    tableName = "PassTag",
    primaryKeys = ["passId", "tagLabel"],
    foreignKeys = [
        ForeignKey(
            entity = Pass::class,
            parentColumns = ["id"],
            childColumns = ["passId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["label"],
            childColumns = ["tagLabel"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("passId"),
        Index("tagLabel"),
    ],
)
data class PassTagCrossRef(
    val passId: String,
    val tagLabel: String,
)
