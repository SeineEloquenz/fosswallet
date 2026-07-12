package nz.eloque.foss_wallet.ui.card

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

private val RoundedCornerRadius = 12.dp

val GenericPassShape = RoundedCornerShape(RoundedCornerRadius)

val StoreCardShape = RoundedCornerShape(RoundedCornerRadius)

class BoardingPassShape(
    val offsetY: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val cutoutRadius = with(density) { 5.dp.toPx() }
        val cornerRadius = with(density) { RoundedCornerRadius.toPx() }
        val hideCutout = offsetY > size.height - cornerRadius - cutoutRadius

        fun Path.corner(
            offset: Offset,
            startAngleDegrees: Float,
        ) = arcTo(
            rect = Rect(offset, cornerRadius),
            startAngleDegrees = startAngleDegrees,
            sweepAngleDegrees = 90f,
            forceMoveTo = false,
        )

        fun Path.cutout(
            offset: Offset,
            startAngleDegrees: Float,
        ) = arcTo(
            rect = Rect(offset, cutoutRadius),
            startAngleDegrees = startAngleDegrees,
            sweepAngleDegrees = -180f,
            forceMoveTo = false,
        )

        return Outline.Generic(
            Path().apply {
                corner(Offset(cornerRadius, cornerRadius), 180f)
                corner(Offset(size.width - cornerRadius, cornerRadius), 270f)
                if (!hideCutout) cutout(Offset(size.width, offsetY), 270f)
                corner(Offset(size.width - cornerRadius, size.height - cornerRadius), 0f)
                corner(Offset(cornerRadius, size.height - cornerRadius), 90f)
                if (!hideCutout) cutout(Offset(0f, offsetY), 90f)
                close()
            },
        )
    }
}

class EventTicketShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val radius = with(density) { 40.dp.toPx() }

        return Outline.Generic(
            Path().apply {
                moveTo(0f, 0f)
                arcTo(
                    rect = Rect(Offset(size.width / 2, -radius * sin(PI / 4).toFloat()), radius),
                    startAngleDegrees = 135f,
                    sweepAngleDegrees = -90f,
                    forceMoveTo = false,
                )
                lineTo(size.width, 0f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            },
        )
    }
}

class CouponShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val radius = with(density) { 4.dp.toPx() }
        val minSpacing = 0.5f * radius

        val numberOfCuts = ((size.width - minSpacing) / (2 * radius + minSpacing)).toInt()
        val remainingSpace = (size.width - minSpacing) % (2 * radius + minSpacing)
        val spacing = minSpacing + remainingSpace / (numberOfCuts + 1)

        fun Path.cutout(ltr: Boolean) {
            var offsetX = radius + spacing
            while (offsetX < size.width) {
                arcTo(
                    rect = Rect(if (ltr) Offset(offsetX, 0f) else Offset(size.width - offsetX, size.height), radius),
                    startAngleDegrees = if (ltr) 180f else 0f,
                    sweepAngleDegrees = -180f,
                    forceMoveTo = false,
                )
                offsetX += 2 * radius + spacing
            }
        }

        return Outline.Generic(
            Path().apply {
                moveTo(0f, 0f)
                cutout(true)
                lineTo(size.width, 0f)
                lineTo(size.width, size.height)
                cutout(false)
                lineTo(0f, size.height)
                close()
            },
        )
    }
}

@Composable
private fun SurfaceWrapper(shape: Shape) =
    Surface(
        modifier = Modifier.padding(8.dp).size(150.dp),
        shape = shape,
        color = Color.Magenta,
    ) { }

@Preview(showBackground = true)
@Composable
private fun GenericPassShapePreview() {
    SurfaceWrapper(GenericPassShape)
}

@Preview(showBackground = true)
@Composable
private fun BoardingPassShapePreview() {
    SurfaceWrapper(BoardingPassShape(with(LocalDensity.current) { 75.dp.toPx() }))
}

@Preview(showBackground = true)
@Composable
private fun EventTicketShapePreview() {
    SurfaceWrapper(EventTicketShape())
}

@Preview(showBackground = true)
@Composable
private fun CouponShapePreview() {
    SurfaceWrapper(CouponShape())
}
