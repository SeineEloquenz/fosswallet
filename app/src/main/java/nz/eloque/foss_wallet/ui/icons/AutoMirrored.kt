package nz.eloque.foss_wallet.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val Icons.AutoMirrored.Filled.Flight: ImageVector
    get() {
        if (flight != null) {
            return flight!!
        }
        flight =
            materialIcon(name = "AutoMirrored.Filled.Flight", autoMirror = true) {
                materialPath {
                    moveTo(8.0f, 21.0f)
                    horizontalLineToRelative(2.0f)
                    lineToRelative(5.0f, -8.0f)
                    horizontalLineTo(20.5f)
                    curveToRelative(0.83f, 0.0f, 1.5f, -0.67f, 1.5f, -1.5f)
                    reflectiveCurveTo(24f - 2.67f, 10.0f, 24f - 3.5f, 10.0f)
                    horizontalLineTo(15.0f)
                    lineToRelative(-5.0f, -8.0f)
                    horizontalLineToRelative(-2.0f)
                    lineToRelative(2.5f, 8.0f)
                    horizontalLineTo(5.0f)
                    lineToRelative(-1.5f, -2.0f)
                    horizontalLineTo(2.0f)
                    lineToRelative(1.0f, 3.5f)
                    lineToRelative(-1.0f, 3.5f)
                    horizontalLineToRelative(1.5f)
                    lineTo(5.0f, 13.0f)
                    horizontalLineToRelative(5.5f)
                    lineToRelative(-2.5f, 8.0f)
                    close()
                }
            }
        return flight!!
    }

private var flight: ImageVector? = null
