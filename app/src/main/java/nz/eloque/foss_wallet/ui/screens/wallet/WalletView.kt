package nz.eloque.foss_wallet.ui.screens.wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.SortOption
import nz.eloque.foss_wallet.model.SortOptionSaver
import nz.eloque.foss_wallet.ui.card.ShortPassCard
import nz.eloque.foss_wallet.ui.components.FilterBar
import nz.eloque.foss_wallet.ui.components.GroupCard
import nz.eloque.foss_wallet.ui.components.SelectionMenu
import nz.eloque.foss_wallet.ui.components.SwipeToDismiss

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletView(
    navController: NavController,
    passViewModel: PassViewModel,
    modifier: Modifier = Modifier,
    emptyIcon: ImageVector = Icons.Default.Wallet,
    showArchived: Boolean = false,
    listState: LazyListState = rememberLazyListState(),
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
    selectedPasses: SnapshotStateSet<Pass>,
) {
    val context = LocalContext.current
    val walletState = passViewModel.uiState.collectAsState()
    val passes = walletState.value.passes.filter { showArchived == it.archived }

    val sortOption = rememberSaveable(stateSaver = SortOptionSaver) { mutableStateOf<SortOption>(SortOption.TimeAdded) }

    if (passes.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                imageVector = emptyIcon,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
                contentDescription = stringResource(R.string.wallet),
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth(0.5f),
                alpha = 0.25f
            )
        }
    }

    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement
            .spacedBy(8.dp),
        contentPadding = WindowInsets.navigationBars.asPaddingValues(),
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        item {
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
            }
        }
        val sortedPasses = passes.sortedWith(sortOption.value.comparator).groupBy { it.groupId }.toList()
        val groups = sortedPasses.filter { it.first != null }
        val ungrouped = sortedPasses.filter { it.first == null }.flatMap { it.second }
        items(groups) { (groupId, passes) ->
            GroupCard(
                groupId!!,
                passes,
                onClick = {
                    navController.navigate("pass/${it.id}")
                },
                passViewModel = passViewModel,
                selectedPasses = selectedPasses
            )
        }
        items(ungrouped) { pass ->
            SwipeToDismiss(
                leftSwipeIcon = Icons.Default.SelectAll,
                allowRightSwipe = false,
                onLeftSwipe = { if (selectedPasses.contains(pass)) selectedPasses.remove(pass) else selectedPasses.add(pass) },
                onRightSwipe = { },
                modifier = Modifier.padding(2.dp)
            ) {
                ShortPassCard(
                    pass = pass,
                    onClick = { navController.navigate("pass/${pass.id}") },
                    selected = selectedPasses.contains(pass)
                )
            }
        }
        item {
            Spacer(modifier = Modifier.padding(4.dp))
        }
    }
}
