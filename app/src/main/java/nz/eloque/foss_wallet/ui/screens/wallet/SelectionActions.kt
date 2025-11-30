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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.share.share
import nz.eloque.foss_wallet.utils.isScrollingUp

@Composable
fun SelectionActions(
    isArchive: Boolean,
    selectedPasses: SnapshotStateSet<Pass>,
    listState: LazyListState,
    passViewModel: PassViewModel,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        FloatingActionButton(
            containerColor = MaterialTheme.colorScheme.error,
            onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    selectedPasses.forEach { passViewModel.delete(it) }
                    selectedPasses.clear()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, context.getString(R.string.pass_deleted), Toast.LENGTH_SHORT).show()
                    }
                }
            },
        ) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
        }
        if (isArchive) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch(Dispatchers.IO) {
                        selectedPasses.forEach { passViewModel.unarchive(it) }
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
                        selectedPasses.forEach { passViewModel.archive(it) }
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
                    share(selectedPasses, context)
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
                    passViewModel.group(selectedPasses.toSet())
                    selectedPasses.clear()
                }
            },
        )
    }
}
