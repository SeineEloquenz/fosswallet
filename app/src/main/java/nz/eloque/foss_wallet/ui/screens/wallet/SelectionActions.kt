package nz.eloque.foss_wallet.ui.screens.wallet

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.LocalizedPassWithTags
import nz.eloque.foss_wallet.share.share
import nz.eloque.foss_wallet.utils.isScrollingUp

@Composable
fun SelectionActions(
    isArchive: Boolean,
    selectedPasses: SnapshotStateSet<LocalizedPassWithTags>,
    listState: LazyListState,
    walletViewModel: WalletViewModel,
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val coroutineScope = rememberCoroutineScope()
    val showDeleteDialog = remember { mutableStateOf(false) }

    fun deleteSelected() {
        coroutineScope.launch(Dispatchers.IO) {
            selectedPasses.toList().forEach { walletViewModel.delete(it.pass) }
            selectedPasses.clear()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, resources.getString(R.string.pass_deleted), Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (showDeleteDialog.value) {
        DeleteConfirmationDialog(
            settingsStore = walletViewModel.settingsStore,
            onConfirm = {
                showDeleteDialog.value = false
                deleteSelected()
            },
            onDismiss = { showDeleteDialog.value = false }
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        FloatingActionButton(
            containerColor = MaterialTheme.colorScheme.error,
            onClick = {
                showDeleteDialog.value = true
            },
        ) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
        }
        if (isArchive) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch(Dispatchers.IO) {
                        selectedPasses.forEach { walletViewModel.unarchive(it.pass) }
                        selectedPasses.clear()
                    }
                },
            ) {
                Icon(imageVector = Icons.Default.Unarchive, contentDescription = stringResource(R.string.unarchive))
            }
        } else {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch(Dispatchers.IO) {
                        selectedPasses.forEach { walletViewModel.archive(it.pass) }
                        selectedPasses.clear()
                    }
                },
            ) {
                Icon(imageVector = Icons.Default.Archive, contentDescription = stringResource(R.string.archive))
            }
        }
        FloatingActionButton(
            onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    share(selectedPasses.map { it.pass }, context)
                }
            },
        ) {
            Icon(imageVector = Icons.Default.Share, contentDescription = stringResource(R.string.share_passes))
        }
        ExtendedFloatingActionButton(
            text = { Text(stringResource(R.string.group)) },
            icon = { Icon(imageVector = Icons.Default.Folder, contentDescription = stringResource(R.string.group)) },
            expanded = listState.isScrollingUp(),
            onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    walletViewModel.group(selectedPasses.map { it.pass }.toSet())
                    selectedPasses.clear()
                }
            },
        )
    }
}
