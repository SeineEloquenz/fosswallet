package nz.eloque.foss_wallet.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A layout composable that places its children in a row. Unlike [Row] each child gets an equal
 * share of the total width to prevent preceding siblings from taking up the entire width. If a
 * child's width is smaller it only gets the width it needs and the remaining space is allocated to
 * the other siblings. This ensures that each child is visible and an overflow is prevented as much
 * as possible.
 *
 * @param modifier The modifier to be applied to the row.
 * @param spacing The space between each child.
 * @param arrangeWithSpaceBetween Arranges the children so that they are spaced evenly while taking
 *   into account the parameter [spacing] as the minimum space between two children.
 * @param content The content of the row.
 */
@Composable
fun FairRow(
    modifier: Modifier = Modifier,
    spacing: Dp = 0.dp,
    arrangeWithSpaceBetween: Boolean = true,
    content: @Composable () -> Unit
) {
    Layout(content, modifier) { measurables, constraints ->
        if (measurables.isEmpty()) return@Layout layout(0, 0) { }

        val spacingPx = spacing.roundToPx()
        val totalSpacing = spacingPx * (measurables.size - 1).coerceAtMost(constraints.maxWidth)

        val intrinsicWidths = measurables.map { it.maxIntrinsicWidth(constraints.maxHeight) }
        val maxWidth = findMaxAllowedWidth(intrinsicWidths, constraints.maxWidth - totalSpacing)

        val placeableContraints = Constraints(maxWidth = maxWidth, maxHeight = constraints.maxHeight)
        val placeables = measurables.map { it.measure(placeableContraints) }
        val placeablesWidth = placeables.sumOf { it.width } + totalSpacing

        val layoutWidth = if (arrangeWithSpaceBetween) constraints.maxWidth else placeablesWidth
        val layoutHeight = placeables.maxOf { it.height }
        val itemSpace = spacingPx + if (arrangeWithSpaceBetween && measurables.size > 1) {
            (constraints.maxWidth - placeablesWidth) / (measurables.size - 1)
        } else 0

        layout(layoutWidth, layoutHeight) {
            var xPosition = 0
            placeables.forEach {
                it.placeRelative(xPosition, 0)
                xPosition += it.width + itemSpace
            }
        }
    }
}

/**
 * Finds the maximum allowed width for the items in a [FairRow] so that the desired width of each
 * item is respected as much as possible.
 *
 * Begins by first giving each item an equal share of the total width. Smaller items use their
 * desired width and the leftover space is given to the remaining items. This is repeated until
 * either all remaining items are smaller than the current maximum allowed width or all are larger.
 *
 * @param intrinsicWidths List of the intrinsic widths of the items.
 * @param totalWidth The available width for all items in [intrinsicWidths].
 * @return The maximum allowed width of an item.
 */
fun findMaxAllowedWidth(intrinsicWidths: List<Int>, totalWidth: Int): Int {
    val maxWidth = totalWidth / intrinsicWidths.size
    val (acceptedWidths, remainingWidths) = intrinsicWidths.partition { it <= maxWidth }

    return if (acceptedWidths.isEmpty() || remainingWidths.isEmpty())
        maxWidth
    else
        findMaxAllowedWidth(remainingWidths, totalWidth - acceptedWidths.sum())
}

@Composable
private fun TextWrapper(text: String) {
    Text(
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.border(Dp.Hairline, Color.Black)
    )
}

private val previewTextData = listOf(
    listOf("AAA", "BBB", "CCC"),
    listOf("AAA ".repeat(4), "BBB", "CCC"),
    listOf("AAA ".repeat(10), "BBB", "CCC ".repeat(4)),
    listOf("AAA", "BBB ".repeat(10), "CCC"),
    listOf("AAA ".repeat(10), "BBB ".repeat(10), "CCC"),
)

@Preview(showBackground = true, widthDp = 250)
@Composable
private fun PreviewOneItem() {
    Column {
        previewTextData.take(3).forEach { texts ->
            FairRow {
                TextWrapper(texts.first())
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 250)
@Composable
private fun PreviewTwoItems() {
    Column {
        previewTextData.forEach { texts ->
            FairRow(spacing = 10.dp) {
                texts.take(2).forEach { TextWrapper(it) }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 250)
@Composable
private fun PreviewTwoItemsWithoutSpaceBetween() {
    Column {
        previewTextData.forEach { texts ->
            FairRow(spacing = 10.dp, arrangeWithSpaceBetween = false) {
                texts.take(2).forEach { TextWrapper(it) }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 250)
@Composable
private fun PreviewThreeItems() {
    Column {
        previewTextData.forEach { texts ->
            FairRow(spacing = 10.dp) {
                texts.forEach { TextWrapper(it) }
            }
        }
    }
}
