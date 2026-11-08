package nz.eloque.foss_wallet.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "tag")
data class Tag(
    @PrimaryKey val label: String,
    val color: Color,
) {
    fun contentColor(): Color = if (color.luminance() > 0.5f) Color.Black else Color.White
}
