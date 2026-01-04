package nz.eloque.foss_wallet.ui.screens.wallet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.model.SortOption
import nz.eloque.foss_wallet.model.Tag
import nz.eloque.foss_wallet.ui.components.ChipSelector
import nz.eloque.foss_wallet.ui.components.FilterBar
import nz.eloque.foss_wallet.ui.components.SelectionMenu
import nz.eloque.foss_wallet.ui.components.tag.TagRow

@Composable
fun FilterBlock(
    passViewModel: PassViewModel,
    sortOption: MutableState<SortOption>,
    passTypesToShow: SnapshotStateList<PassType>,
    tags: Set<Tag>,
    tagToFilterFor: MutableState<Tag?>,
) {
    val context = LocalContext.current

    var filtersShown by remember { mutableStateOf(false) }

    Column {


        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            FilterBar(
                onSearch = { passViewModel.filter(it) },
                modifier = Modifier
                    .padding(start = 6.dp, bottom = 6.dp)
                    .weight(1f)
            )
            SelectionMenu(
                icon = Icons.AutoMirrored.Default.Sort,
                contentDescription = R.string.filter,
                options = SortOption.all(),
                selectedOption = sortOption.value,
                onOptionSelected = { sortOption.value = it },
                optionLabel = { context.getString(it.l18n) }
            )
            IconButton(onClick = {
                filtersShown = !filtersShown
            }) {
                if (filtersShown) {
                    Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = stringResource(R.string.collapse))
                } else {
                    Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = stringResource(R.string.expand))
                }
            }
        }

        AnimatedVisibility(
            visible = filtersShown,
            enter = expandVertically(
                animationSpec = tween(durationMillis = 300)
            ) + fadeIn(animationSpec = tween(300)),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = 300)
            ) + fadeOut(animationSpec = tween(300))
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                ChipSelector(
                    options = PassType.all(),
                    selectedOptions = passTypesToShow,
                    onOptionSelected = { passTypesToShow.add(it) },
                    onOptionDeselected = { passTypesToShow.remove(it) },
                    optionLabel = { context.getString(it.label) },
                    modifier = Modifier.fillMaxWidth()
                )
                TagRow(
                    tags = tags,
                    selectedTag = tagToFilterFor.value,
                    onTagSelected = { tagToFilterFor.value = it },
                    onTagDeselected = { tagToFilterFor.value = null },
                    passViewModel = passViewModel,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}