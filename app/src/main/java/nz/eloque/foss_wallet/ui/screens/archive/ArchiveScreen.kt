package nz.eloque.foss_wallet.ui.screens.archive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.ui.WalletScaffold
import nz.eloque.foss_wallet.ui.screens.wallet.PassViewModel
import nz.eloque.foss_wallet.ui.screens.wallet.WalletView
import nz.eloque.foss_wallet.utils.isScrollingUp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    navController: NavHostController,
    passViewModel: PassViewModel,
) {
    val coroutineScope = rememberCoroutineScope()

    val listState = rememberLazyListState()
    val selectedPasses = remember { mutableStateSetOf<Pass>() }

    WalletScaffold(
        navController = navController,
        title = stringResource(id = R.string.archive),
        toolWindow = true,
        floatingActionButton = {
            if (selectedPasses.isNotEmpty()) {
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
                            }
                        },
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                    }
                    FloatingActionButton(
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                selectedPasses.forEach { passViewModel.unarchive(it) }
                                selectedPasses.clear()
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Unarchive, contentDescription = stringResource(R.string.unarchive))
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
        },
    ) { scrollBehavior ->
        WalletView(
            navController,
            passViewModel,
            showArchived = true,
            emptyIcon = Icons.Default.Archive,
            listState = listState,
            scrollBehavior = scrollBehavior,
            selectedPasses = selectedPasses
        )
    }
}