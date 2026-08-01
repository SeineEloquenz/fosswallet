package nz.eloque.foss_wallet.ui.screens.wallet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import nz.eloque.compose_kit.components.ExtendedSelectionMenu
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.model.Tag
import nz.eloque.foss_wallet.ui.components.tag.TagRow

@Composable
fun FilterBlock(
    walletViewModel: WalletViewModel,
    tags: Set<Tag>,
) {
    val resources = LocalResources.current

    val sortOption by walletViewModel.sortOptionState.collectAsState()
    val selectedPassTypes by walletViewModel.selectedPassTypes.collectAsState()
    val tagToFilterFor by walletViewModel.tagFilter.collectAsState()

    ExtendedSelectionMenu(
        singleOptions = SortOption.all(),
        multiOptions = PassType.all(),
        singleOptionLabel = { resources.getString(it.l18n) },
        multiOptionLabel = { resources.getString(it.label) },
        selectedSingleOption = sortOption,
        selectedMultiOptions = selectedPassTypes.toList(),
        onSingleOptionSelected = { walletViewModel.setSortOption(it) },
        onMultiOptionSelected = { walletViewModel.selectPassType(it) },
        onMultiOptionDeselected = { walletViewModel.deselectPassType(it) },
        contentDescription = stringResource(R.string.filter),
    )
    TagRow(
        tags = tags,
        selectedTag = tagToFilterFor,
        onTagSelected = { walletViewModel.setTagFilter(it) },
        onTagDeselected = { walletViewModel.setTagFilter(null) },
        walletViewModel = walletViewModel,
    )
}
