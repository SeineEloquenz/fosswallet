@file:OptIn(ExperimentalMaterial3Api::class)

package nz.eloque.foss_wallet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.carousel.CarouselDefaults
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.utils.darken

@Composable
fun GroupCard(
    groupId: Long,
    passes: List<Pass>,
    modifier: Modifier = Modifier,
    onClick: ((Pass) -> Unit)? = null,
) {
    ElevatedCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.padding(10.dp)
        ) {
            val carouselState = rememberCarouselState(0) { passes.size }
            HorizontalUncontainedCarousel(
                state = carouselState,
                itemSpacing = 25.dp,
                itemWidth = 500.dp,
                flingBehavior = CarouselDefaults.singleAdvanceFlingBehavior(carouselState),
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(top = 16.dp, bottom = 16.dp),
            ) { index ->
                val item = passes[index]
                PassCard(
                    pass = item,
                    colors = CardDefaults.elevatedCardColors().copy(containerColor = CardDefaults.elevatedCardColors().containerColor.darken(1.25f)),
                    onClick = { onClick?.invoke(item) },
                ) {
                }
            }
            SelectionIndicator(
                -1,
                passes.size,
            )
        }
    }
}

@Composable
private fun SelectionIndicator(
    selectedItem: Int,
    itemCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().padding(bottom = 8.dp)
    ) {
        repeat(itemCount) { index ->
            val isSelected = index == selectedItem
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(if (isSelected) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f))
            )
        }
    }
}