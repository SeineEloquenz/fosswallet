package nz.eloque.foss_wallet.ui.screens.wallet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.runtime.toMutableStateList
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
import kotlinx.coroutines.flow.map
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.LocalizedPassWithTags
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.model.SortOption
import nz.eloque.foss_wallet.model.SortOptionSaver
import nz.eloque.foss_wallet.model.Tag
import nz.eloque.foss_wallet.ui.card.ShortPassCard
import nz.eloque.foss_wallet.ui.components.ChipSelector
import nz.eloque.foss_wallet.ui.components.FilterBar
import nz.eloque.foss_wallet.ui.components.GroupCard
import nz.eloque.foss_wallet.ui.components.SelectionMenu
import nz.eloque.foss_wallet.ui.components.SwipeToDismiss
import nz.eloque.foss_wallet.ui.components.tag.TagRow

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
    val context = LocalContext.current

    val passFlow = passViewModel.filteredPasses
    val passes: List<LocalizedPassWithTags> by remember(passFlow) { passFlow }.map { passes -> passes.filter { archive == it.pass.archived } }.collectAsState(listOf())

    val tagFlow = passViewModel.allTags
    val tags by tagFlow.collectAsState(setOf())

    val passTypesToShow = remember { PassType.all().toMutableStateList() }

    val sortOption = rememberSaveable(stateSaver = SortOptionSaver) { mutableStateOf(SortOption.TimeAdded) }

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
    } else {
        var filtersShown by remember { mutableStateOf(false) }
        var tagToFilterFor by remember { mutableStateOf<Tag?>(null) }

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
            }
            item {
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
                            selectedTag = tagToFilterFor,
                            onTagSelected = { tagToFilterFor = it },
                            onTagDeselected = { tagToFilterFor = null },
                            passViewModel = passViewModel,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            val sortedPasses = passes
                .filter { localizedPass -> passTypesToShow.any { localizedPass.pass.type.isSameType(it) } }
                .filter { localizedPass -> tagToFilterFor == null || localizedPass.tags.contains(tagToFilterFor) }
                .sortedWith(sortOption.value.comparator)
                .groupBy { it.pass.groupId }.toList()
            val groups = sortedPasses.filter { it.first != null }
            val ungrouped = sortedPasses.filter { it.first == null }.flatMap { it.second }
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
}