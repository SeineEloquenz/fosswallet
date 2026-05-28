package nz.eloque.foss_wallet.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.SearchArchive: ImageVector
    get() =
        ImageVector
            .Builder(
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                path(fill = SolidColor(Color.Unspecified)) {
                    // Search glass outline
                    moveTo(15.5f, 14f)
                    horizontalLineTo(14.71f)
                    lineTo(14.43f, 13.73f)
                    arcTo(6.471f, 6.471f, 0f, isMoreThanHalf = false, isPositiveArc = false, 16f, 9.5f)
                    arcTo(6.5f, 6.5f, 0f, isMoreThanHalf = true, isPositiveArc = false, 9.5f, 16f)
                    curveTo(11.11f, 16f, 12.59f, 15.41f, 13.73f, 14.43f)
                    lineTo(14f, 14.71f)
                    verticalLineTo(15.5f)
                    lineTo(19f, 20.49f)
                    lineTo(20.49f, 19f)
                    lineTo(15.5f, 14f)
                    close()
                    // Inner circle cutout
                    moveTo(9.5f, 14f)
                    curveTo(7.01f, 14f, 5f, 11.99f, 5f, 9.5f)
                    curveTo(5f, 7.01f, 7.01f, 5f, 9.5f, 5f)
                    curveTo(11.99f, 5f, 14f, 7.01f, 14f, 9.5f)
                    curveTo(14f, 11.99f, 11.99f, 14f, 9.5f, 14f)
                    close()
                }
                group(
                    translationX = 6.86f,
                    translationY = 6.86f,
                    scaleX = 0.22f,
                    scaleY = 0.22f,
                ) {
                    path(fill = SolidColor(Color.Unspecified)) {
                        // Archive icon
                        moveTo(20.54f, 5.23f)
                        lineTo(19.15f, 3.55f)
                        curveTo(18.88f, 3.21f, 18.47f, 3f, 18f, 3f)
                        horizontalLineTo(6f)
                        curveTo(5.53f, 3f, 5.12f, 3.21f, 4.84f, 3.55f)
                        lineTo(3.46f, 5.23f)
                        curveTo(3.17f, 5.57f, 3f, 6.02f, 3f, 6.5f)
                        verticalLineTo(19f)
                        curveTo(3f, 20.1f, 3.9f, 21f, 5f, 21f)
                        horizontalLineTo(19f)
                        curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
                        verticalLineTo(6.5f)
                        curveTo(21f, 6.02f, 20.83f, 5.57f, 20.54f, 5.23f)
                        close()
                        // Down arrow
                        moveTo(12f, 17.5f)
                        lineTo(6.5f, 12f)
                        horizontalLineTo(10f)
                        verticalLineTo(10f)
                        horizontalLineTo(14f)
                        verticalLineTo(12f)
                        horizontalLineTo(17.5f)
                        lineTo(12f, 17.5f)
                        close()
                        // Top bar
                        moveTo(5.12f, 5f)
                        lineTo(5.93f, 4f)
                        horizontalLineTo(17.93f)
                        lineTo(18.87f, 5f)
                        horizontalLineTo(5.12f)
                        close()
                    }
                }
            }.build()
