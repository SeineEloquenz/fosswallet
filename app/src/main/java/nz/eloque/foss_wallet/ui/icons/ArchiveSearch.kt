package nz.eloque.foss_wallet.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path

val Icons.Filled.ArchiveSearch: ImageVector
    get() {
        if (archiveSearch != null) {
            return archiveSearch!!
        }
        archiveSearch =
            materialIcon(name = "Filled.ArchiveSearch") {
                materialPath(pathFillType = PathFillType.EvenOdd) {
                    moveTo(20.54f, 5.23f)
                    lineToRelative(-1.39f, -1.68f)
                    curveTo(18.88f, 3.21f, 18.47f, 3f, 18f, 3f)
                    horizontalLineTo(6f)
                    curveToRelative(-0.47f, 0f, -0.88f, 0.21f, -1.16f, 0.55f)
                    lineTo(3.46f, 5.23f)
                    curveTo(3.17f, 5.57f, 3f, 6.02f, 3f, 6.5f)
                    verticalLineTo(19f)
                    curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
                    horizontalLineToRelative(14f)
                    curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                    verticalLineTo(6.5f)
                    curveToRelative(0f, -0.48f, -0.17f, -0.93f, -0.46f, -1.27f)
                    close()

                    moveTo(5.12f, 5f)
                    lineToRelative(0.81f, -1f)
                    horizontalLineToRelative(12f)
                    lineToRelative(0.94f, 1f)
                    horizontalLineTo(5.12f)
                    close()
                }

                group(
                    pivotX = 11.745f,
                    pivotY = 11f,
                    scaleX = 0.4375f,
                    scaleY = 0.4375f,
                    translationX = 0.255f,
                    translationY = 1.75f,
                ) {
                    path(fill = SolidColor(Color(0xFF1C1B1F))) {
                        moveTo(15.5f, 14f)
                        horizontalLineToRelative(-0.79f)
                        lineToRelative(-0.28f, -0.27f)
                        curveTo(15.41f, 12.59f, 16f, 11.11f, 16f, 9.5f)
                        curveTo(16f, 5.91f, 13.09f, 3f, 9.5f, 3f)
                        reflectiveCurveTo(3f, 5.91f, 3f, 9.5f)
                        reflectiveCurveTo(5.91f, 16f, 9.5f, 16f)
                        curveToRelative(1.61f, 0f, 3.09f, -0.59f, 4.23f, -1.57f)
                        lineToRelative(0.27f, 0.28f)
                        verticalLineToRelative(0.79f)
                        lineToRelative(5f, 4.99f)
                        lineTo(20.49f, 19f)
                        lineToRelative(-4.99f, -5f)
                        close()

                        moveTo(9.5f, 14f)
                        curveTo(7.01f, 14f, 5f, 11.99f, 5f, 9.5f)
                        reflectiveCurveTo(7.01f, 5f, 9.5f, 5f)
                        reflectiveCurveTo(14f, 7.01f, 14f, 9.5f)
                        reflectiveCurveTo(11.99f, 14f, 9.5f, 14f)
                        close()
                    }
                }
            }
        return archiveSearch!!
    }

private var archiveSearch: ImageVector? = null
