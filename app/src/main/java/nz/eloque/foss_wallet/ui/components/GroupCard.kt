package nz.eloque.foss_wallet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderDelete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.LocalizedPassWithTags
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.Tag
import nz.eloque.foss_wallet.share.share
import nz.eloque.foss_wallet.ui.card.ShortPassCard
import nz.eloque.foss_wallet.ui.screens.wallet.WalletViewModel

@Composable
fun GroupCard(
    groupId: Long,
    passes: List<LocalizedPassWithTags>,
    allTags: Set<Tag>,
    selectedPasses: MutableSet<LocalizedPassWithTags>,
    walletViewModel: WalletViewModel,
    modifier: Modifier = Modifier,
    onClick: (Pass) -> Unit = {},
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    ElevatedCard(
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.padding(10.dp)
        ) {
            val pagerState = rememberPagerState(
                initialPage = 0, 
                pageCount = { passes.size }
            )
            HorizontalPager(
                state = pagerState,
                pageSpacing = 28.dp,
            ) { index ->
                val item = passes[index]
                ShortPassCard(
                    pass = item,
                    allTags = allTags,
                    onClick = {
                        if (selectedPasses.isNotEmpty()) {
                            val allGroupPassesSelected = passes.all { selectedPasses.contains(it) }
                            if (allGroupPassesSelected) {
                                selectedPasses.removeAll(passes.toSet())
                            } else {
                                selectedPasses.addAll(passes)
                            }
                        } else {
                            onClick.invoke(item.pass)
                        }
                    },
                    onLongClick = {
                        val allGroupPassesSelected = passes.all { selectedPasses.contains(it) }
                        if (allGroupPassesSelected) {
                            selectedPasses.removeAll(passes.toSet())
                        } else {
                            selectedPasses.addAll(passes)
                        }
                    },
                    selected = selectedPasses.contains(item),
                    toned = true,
                    barcodePosition = walletViewModel.barcodePosition(),
                    increaseBrightness = walletViewModel.increasePassViewBrightness()
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                SelectionIndicator(pagerState.currentPage, passes.size, Modifier.align(Alignment.Center))
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    if (selectedPasses.isNotEmpty()) {
                        IconButton(onClick = { coroutineScope.launch(Dispatchers.IO) { groupId.let {
                            walletViewModel.associate(groupId, selectedPasses.map { it.pass }.toSet())
                            selectedPasses.clear()
                        } } }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.ungroup))
                        }
                    }
                    IconButton(onClick = {
                        val selectedPass = passes[pagerState.currentPage]
                        coroutineScope.launch(Dispatchers.IO) { groupId.let { walletViewModel.dissociate(selectedPass.pass, groupId) } }
                    }) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = stringResource(R.string.ungroup))
                    }

                    val expanded = remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                    ) {
                        IconButton(onClick = { expanded.value = !expanded.value }) {
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more_options))
                        }
                        DropdownMenu(
                            expanded = expanded.value,
                            onDismissRequest = { expanded.value = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.share_passes)) },
                                leadingIcon = { Icon(imageVector = Icons.Default.Share, contentDescription = stringResource(R.string.share_passes)) },
                                onClick = { coroutineScope.launch(Dispatchers.IO) { share(passes.map { it.pass }, context) } }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.ungroup)) },
                                leadingIcon = { Icon(imageVector = Icons.Default.FolderDelete, contentDescription = stringResource(R.string.ungroup)) },
                                onClick = { coroutineScope.launch(Dispatchers.IO) { groupId.let { walletViewModel.deleteGroup(it) } } }
                            )
                        }
                    }
                }
            }
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
        modifier = modifier
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
