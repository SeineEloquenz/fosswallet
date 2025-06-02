package nz.eloque.foss_wallet.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.CarouselDefaults
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.utils.darken

@OptIn(ExperimentalMaterial3Api::class)
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Folder, contentDescription = stringResource(R.string.passes))
                Text(
                    text = "${passes.size} ${stringResource(R.string.passes)}",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
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
        }
    }
}