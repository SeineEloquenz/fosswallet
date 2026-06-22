package nz.eloque.foss_wallet.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "tag")
data class Tag(
    @PrimaryKey val label: String,
    @Contextual val color: Color,
) {
    fun contentColor(): Color = if (color.luminance() > 0.5f) Color.Black else Color.White
}
