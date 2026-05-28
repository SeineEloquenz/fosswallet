package nz.eloque.foss_wallet.ui.screens.wallet

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.model.SortOption
import nz.eloque.foss_wallet.model.Tag
import nz.eloque.foss_wallet.ui.components.SelectionMenu
import nz.eloque.foss_wallet.ui.components.tag.TagRow

@Composable
fun FilterBlock(
    walletViewModel: WalletViewModel,
    tags: Set<Tag>,
) {
    val resources = LocalResources.current
    
    val sortOption = walletViewModel.sortOptionState.collectAsState().value
    val passTypesToShow = remember { PassType.all().toMutableStateList() }
    val tagToFilterFor = remember { mutableStateOf<Tag?>(null) }

    SelectionMenu(
        singleOptions = SortOption.all(),
        multiOptions = PassType.all(),
        singleOptionLabel = { resources.getString(it.l18n) },
        multiOptionLabel = { resources.getString(it.label) },
        selectedSingleOption = sortOption,
        selectedMultiOptions = passTypesToShow,
        onSingleOptionSelected = { walletViewModel.setSortOption(it) },
        onMultiOptionSelected = { passTypesToShow.add(it) },
        onMultiOptionDeselected = { passTypesToShow.remove(it) },
        contentDescription = R.string.filter,
    )
    TagRow(
        tags = tags,
        selectedTag = tagToFilterFor.value,
        onTagSelected = { tagToFilterFor.value = it },
        onTagDeselected = { tagToFilterFor.value = null },
        walletViewModel = walletViewModel,
        modifier = Modifier,
    )
}
