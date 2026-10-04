package nz.eloque.foss_wallet.ui.screens.wallet

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Deselect
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import nz.eloque.compose_kit.fab.FabMenu
import nz.eloque.compose_kit.fab.FabMenuItem
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.LocalizedPassWithTags
import nz.eloque.foss_wallet.ui.ImportEventsEffect
import nz.eloque.foss_wallet.ui.Route
import nz.eloque.foss_wallet.ui.WalletScaffold
import nz.eloque.foss_wallet.utils.PkpassMimeTypes

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    navController: NavHostController,
    walletViewModel: WalletViewModel = hiltViewModel(),
) {
    val listState = rememberLazyListState()

    val loading by walletViewModel.importing.collectAsState()

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            walletViewModel.import(uris)
        }
    ImportEventsEffect(walletViewModel, navController)
    val selectedPasses = remember { mutableStateSetOf<LocalizedPassWithTags>() }
    val visiblePasses = remember { mutableStateOf<Set<LocalizedPassWithTags>>(emptySet()) }
    val allVisibleSelected = visiblePasses.value.isNotEmpty() && visiblePasses.value.all { selectedPasses.contains(it) }

    WalletScaffold(
        navController = navController,
        title = stringResource(id = R.string.wallet),
        actions = {
            if (selectedPasses.isNotEmpty()) {
                IconButton(
                    onClick = {
                        if (allVisibleSelected) {
                            selectedPasses.removeAll(visiblePasses.value)
                        } else {
                            selectedPasses.addAll(visiblePasses.value)
                        }
                    },
                    enabled = visiblePasses.value.isNotEmpty(),
                ) {
                    Icon(
                        imageVector = if (allVisibleSelected) Icons.Outlined.Deselect else Icons.Outlined.SelectAll,
                        contentDescription =
                            if (allVisibleSelected) {
                                stringResource(R.string.clear_selection)
                            } else {
                                stringResource(R.string.select_all)
                            },
                    )
                }
            }
            IconButton(onClick = {
                navController.navigate(Route.Archive)
            }) {
                Icon(
                    imageVector = Icons.Default.Archive,
                    contentDescription = stringResource(R.string.the_archive),
                )
            }
            IconButton(onClick = {
                navController.navigate(Route.Settings)
            }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings),
                )
            }
        },
        floatingActionButton = {
            if (selectedPasses.isNotEmpty()) {
                SelectionActions(
                    false,
                    selectedPasses,
                    listState,
                    walletViewModel,
                )
            } else {
                FabMenu(
                    items =
                        listOf(
                            FabMenuItem(
                                icon = Icons.Default.MoreHoriz,
                                title = stringResource(R.string.advanced),
                                onClick = {
                                    navController.navigate(Route.AdvancedAdd)
                                },
                            ),
                            FabMenuItem(
                                icon = Icons.Default.QrCodeScanner,
                                title = stringResource(R.string.barcode),
                                onClick = {
                                    navController.navigate(Route.Scan)
                                },
                            ),
                            FabMenuItem(
                                icon = Icons.Default.Add,
                                title = stringResource(R.string.import_pass),
                                onClick = {
                                    launcher.launch(
                                        arrayOf(
                                            "application/json+zip",
                                            "application/octet-stream",
                                            "text/json",
                                        ).plus(PkpassMimeTypes),
                                    )
                                },
                            ),
                        ),
                )
            }
        },
    ) { scrollBehavior ->
        WalletView(
            navController = navController,
            walletViewModel = walletViewModel,
            listState = listState,
            scrollBehavior = scrollBehavior,
            selectedPasses = selectedPasses,
            onVisiblePassesChanged = { visiblePasses.value = it },
        )

        if (loading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
