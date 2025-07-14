package nz.eloque.foss_wallet.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector


val Icons.AutoMirrored.Filled.FlightTakeoff: ImageVector
    get() {
        if (flightTakeoff != null) {
            return flightTakeoff!!
        }
        flightTakeoff = materialIcon(name = "AutoMirrored.Filled.AirplaneTicket", autoMirror =
            true) {
            materialPath {
                moveTo(2.5f, 19.0f)
                horizontalLineToRelative(19.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-19.0f)
                verticalLineTo(19.0f)
                close()
                moveTo(22.07f, 9.64f)
                curveToRelative(-0.21f, -0.8f, -1.04f, -1.28f, -1.84f, -1.06f)
                lineTo(14.92f, 10.0f)
                lineToRelative(-6.9f, -6.43f)
                lineTo(6.09f, 4.08f)
                lineToRelative(4.14f, 7.17f)
                lineToRelative(-4.97f, 1.33f)
                lineToRelative(-1.97f, -1.54f)
                lineToRelative(-1.45f, 0.39f)
                lineToRelative(2.59f, 4.49f)
                curveToRelative(0.0f, 0.0f, 7.12f, -1.9f, 16.57f, -4.43f)
                curveTo(21.81f, 11.26f, 22.28f, 10.44f, 22.07f, 9.64f)
                close()
            }
        }
        return flightTakeoff!!
    }

private var flightTakeoff: ImageVector? = null