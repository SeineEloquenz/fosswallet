package nz.eloque.foss_wallet.ui.view.wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderDelete
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.ui.card.PassCard
import nz.eloque.foss_wallet.ui.components.FilterBar
import nz.eloque.foss_wallet.ui.components.GroupCard
import nz.eloque.foss_wallet.ui.components.SwipeToDismiss

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletView(
    navController: NavController,
    passViewModel: PassViewModel,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
    selectedPasses: SnapshotStateSet<Pass>,
) {
    val coroutineScope = rememberCoroutineScope()
    val list = passViewModel.uiState.collectAsState()

    val comparator by remember { mutableStateOf( Comparator<Pass> { left, right ->
        -left.addedAt.compareTo(right.addedAt)
    }) }

    if (list.value.passes.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                imageVector = Icons.Default.Wallet,
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
            .spacedBy(10.dp),
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        item {
            FilterBar(
                onSearch = { passViewModel.filter(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp, end = 6.dp, bottom = 6.dp)
            )
        }
        val sortedPasses = list.value.passes.sortedWith(comparator).groupBy { it.groupId }.toList()
        val groups = sortedPasses.filter { it.first != null }
        val ungrouped = sortedPasses.filter { it.first == null }.flatMap { it.second }
        items(groups) { (groupId, passes) ->
            GroupCard(
                groupId!!,
                passes,
                onClick = {
                    navController.navigate("pass/${it.id}")
                },
                actions = {
                    IconButton(onClick = { coroutineScope.launch(Dispatchers.IO) { groupId.let { passViewModel.deleteGroup(it) } } }
                    ) {
                        Icon(imageVector = Icons.Default.FolderDelete, contentDescription = stringResource(R.string.ungroup))
                    }
                }
            )
        }
        items(ungrouped) { pass ->
            SwipeToDismiss(
                leftSwipeIcon = Icons.Default.SelectAll,
                allowRightSwipe = false,
                onLeftSwipe = { if (selectedPasses.contains(pass)) selectedPasses.remove(pass) else selectedPasses.add(pass) },
                onRightSwipe = { }
            ) {
                PassCard(
                    pass = pass,
                    onClick = {
                        navController.navigate("pass/${pass.id}")
                    },
                    selected = selectedPasses.contains(pass)
                ) {

                }
            }
        }
    }
}