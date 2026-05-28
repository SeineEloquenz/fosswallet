package nz.eloque.foss_wallet.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import nz.eloque.foss_wallet.ui.theme.WalletTheme

@Composable
fun HorizontalPagerIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    maxItems: Int = 7,
    indicatorSize: Dp = 8.dp,
    spacing: Dp = 8.dp,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.secondaryContainer,
) {
    val maxItems = (if (maxItems % 2 == 0) maxItems - 1 else maxItems).coerceAtLeast(3).coerceAtMost(pagerState.pageCount)

    val pageOffsetFraction by remember { derivedStateOf { pagerState.currentPage + pagerState.currentPageOffsetFraction } }
    val scrollOffsetFraction = (pageOffsetFraction - (maxItems / 2)).coerceIn(0f, (pagerState.pageCount - maxItems).toFloat())

    Canvas(modifier.size(width = (indicatorSize + spacing) * (maxItems + 2) - spacing + indicatorSize, height = indicatorSize)) {
        val indicatorSizePx = indicatorSize.toPx()
        val spacingPx = spacing.toPx()

        val firstVisibleItem = (pagerState.currentPage - maxItems / 2).coerceIn(0, pagerState.pageCount - maxItems) - 1
        val lastVisibleItem = firstVisibleItem + maxItems + 1
        (firstVisibleItem..lastVisibleItem).forEach {
            val activeWidthFraction =
                when (it - pageOffsetFraction.toInt()) {
                    0 -> 1 - (pageOffsetFraction % 1)
                    1 -> pageOffsetFraction % 1
                    else -> 0f
                }
            val activeWidthTranslation = if (it > pageOffsetFraction.toInt()) indicatorSizePx * (1 - activeWidthFraction) else 0f

            val isFirstPage = it == 0
            val isLastPage = it == pagerState.pageCount - 1
            val scale =
                when (it - scrollOffsetFraction.toInt()) {
                    maxItems -> (scrollOffsetFraction % 1) / if (isLastPage) 1 else 2
                    maxItems - 1 -> if (isLastPage) 1f else 0.5f + (scrollOffsetFraction % 1) / 2
                    1 -> 1 - (scrollOffsetFraction % 1) / 2
                    0 -> (1 - scrollOffsetFraction % 1) / if (isFirstPage) 1 else 2
                    -1, maxItems + 1 -> 0f
                    else -> 1f
                }

            withTransform({
                if (this@Canvas.layoutDirection == LayoutDirection.Rtl) scale(scaleX = -1f, scaleY = 1f)
                translate(left = (indicatorSizePx + spacingPx) * (it + 1 - scrollOffsetFraction) + activeWidthTranslation)
                scale(scaleX = scale, scaleY = scale, pivot = Offset(indicatorSizePx / 2, indicatorSizePx / 2))
            }) {
                drawRoundRect(
                    color = if (it == pagerState.currentPage) activeColor else inactiveColor,
                    size = Size(indicatorSizePx * (1 + activeWidthFraction), indicatorSizePx),
                    cornerRadius = CornerRadius(indicatorSizePx / 2),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun HorizontalPagerIndicatorPreview() {
    WalletTheme {
        Column(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val pagerState = rememberPagerState { 10 }

            HorizontalPager(
                state = pagerState,
                pageSpacing = 16.dp,
            ) {
                Box(Modifier.fillMaxWidth().height(100.dp).background(MaterialTheme.colorScheme.surfaceContainerHighest))
            }

            HorizontalPagerIndicator(pagerState)
        }
    }
}
