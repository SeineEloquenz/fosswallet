package nz.eloque.foss_wallet.ui.screens.wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.map
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.LocalizedPassWithTags
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.model.SortOption
import nz.eloque.foss_wallet.model.SortOptionSaver
import nz.eloque.foss_wallet.model.Tag
import nz.eloque.foss_wallet.ui.card.ShortPassCard
import nz.eloque.foss_wallet.ui.components.GroupCard
import nz.eloque.foss_wallet.ui.components.SwipeToDismiss

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletView(
    navController: NavController,
    passViewModel: PassViewModel,
    modifier: Modifier = Modifier,
    emptyIcon: ImageVector = Icons.Default.Wallet,
    archive: Boolean = false,
    listState: LazyListState = rememberLazyListState(),
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
    selectedPasses: SnapshotStateSet<LocalizedPassWithTags>,
) {
    val emptyState = rememberLazyListState()
    val passFlow = passViewModel.filteredPasses
    val passes: List<LocalizedPassWithTags> by remember(passFlow) { passFlow }.map { passes -> passes.filter { archive == it.pass.archived } }.collectAsState(listOf())

    val tagFlow = passViewModel.allTags
    val tags by tagFlow.collectAsState(setOf())

    val passTypesToShow = remember { PassType.all().toMutableStateList() }

    val sortOption = rememberSaveable(stateSaver = SortOptionSaver) { mutableStateOf(SortOption.TimeAdded) }

    val tagToFilterFor = remember { mutableStateOf<Tag?>(null) }

    val sortedPasses = passes
        .filter { localizedPass -> passTypesToShow.any { localizedPass.pass.type.isSameType(it) } }
        .filter { localizedPass -> tagToFilterFor.value == null || localizedPass.tags.contains(tagToFilterFor.value) }
        .filter { localizedPass -> !localizedPass.pass.hidden || passViewModel.isAuthenticated }
        .sortedWith(
            compareBy<LocalizedPassWithTags> { !it.pass.pinned }
                .then(sortOption.value.comparator)
        )
        .groupBy { it.pass.groupId }.toList()

    if (sortedPasses.isEmpty()) {
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
        state = if (passes.isEmpty()) emptyState else listState,
        verticalArrangement = Arrangement
            .spacedBy(8.dp),
        contentPadding = WindowInsets.navigationBars.asPaddingValues(),
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        val groups = sortedPasses.filter { it.first != null }
        val ungrouped = sortedPasses.filter { it.first == null }.flatMap { it.second }

        item {
            FilterBlock(
                passViewModel = passViewModel,
                sortOption = sortOption,
                passTypesToShow = passTypesToShow,
                tags = tags,
                tagToFilterFor = tagToFilterFor
            )
        }

        items(groups) { (groupId, passes) ->
            GroupCard(
                groupId = groupId!!,
                passes = passes,
                allTags = tags,
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
                    allTags = tags,
                    onClick = {
                        navController.navigate("pass/${pass.pass.id}")
                    },
                    selected = selectedPasses.contains(pass),
                    barcodePosition = passViewModel.barcodePosition(),
                    increaseBrightness = passViewModel.increasePassViewBrightness()
                )
            }
        }
        item {
            Spacer(modifier = Modifier.padding(4.dp))
        }
    }
}