package nz.eloque.foss_wallet.ui.screens.wallet

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.Deselect
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.LocalizedPassWithTags
import nz.eloque.foss_wallet.persistence.loader.Loader
import nz.eloque.foss_wallet.persistence.loader.LoaderResult
import nz.eloque.foss_wallet.ui.Screen
import nz.eloque.foss_wallet.ui.WalletScaffold
import nz.eloque.foss_wallet.utils.Biometric
import nz.eloque.foss_wallet.ui.components.FabMenu
import nz.eloque.foss_wallet.ui.components.FabMenuItem
import nz.eloque.foss_wallet.utils.PkpassMimeTypes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    navController: NavHostController,
    walletViewModel: WalletViewModel,
) {
    val context = LocalContext.current
    val resources = LocalResources.current

    val contentResolver = context.contentResolver
    val coroutineScope = rememberCoroutineScope()

    val listState = rememberLazyListState()
    val activity = remember(context) { context as FragmentActivity }
    val snackbarHostState = remember { SnackbarHostState() }
    val biometric = remember { Biometric(activity, snackbarHostState, coroutineScope) }

    val loading = remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        println("selected file URI $uris")
        coroutineScope.launch {
            loading.value = true
            withContext(Dispatchers.IO) {
                var result: LoaderResult? = null
                uris.forEach { uri ->
                    contentResolver.openInputStream(uri)?.use {
                        result = Loader(context).handleInputStream(
                            it,
                            walletViewModel,
                            coroutineScope
                        )
                    }
                }
                if (uris.size == 1) {
                    if (result is LoaderResult.Single) {
                        withContext(Dispatchers.Main) {
                            navController.navigate("pass/${result.passId}")
                        }
                    }
                }
            }
            loading.value = false
        }
    }
    val selectedPasses = remember { mutableStateSetOf<LocalizedPassWithTags>() }
    val isAuthenticated by walletViewModel.isAuthenticated.collectAsState()
    val visiblePasses = remember { mutableStateOf<Set<LocalizedPassWithTags>>(emptySet()) }
    val allVisibleSelected = visiblePasses.value.isNotEmpty() && visiblePasses.value.all { selectedPasses.contains(it) }

    WalletScaffold(
        navController = navController,
        title = stringResource(id = Screen.Wallet.resourceId),
        snackbarHostState = snackbarHostState,
        actions = {
            if (isAuthenticated) {
                IconButton(onClick = { walletViewModel.conceal() }) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = stringResource(R.string.conceal)
                    )
                }
            } else {
                IconButton(
                    onClick = {
                        biometric.prompt(
                            description = resources.getString(R.string.reveal),
                            onSuccess = { walletViewModel.reveal() }
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = stringResource(R.string.reveal)
                    )
                }
            }

            if (selectedPasses.isNotEmpty()) {
                IconButton(
                    onClick = {
                        if (allVisibleSelected) {
                            selectedPasses.removeAll(visiblePasses.value)
                        } else {
                            selectedPasses.addAll(visiblePasses.value)
                        }
                    },
                    enabled = visiblePasses.value.isNotEmpty()
                ) {
                    Icon(
                        imageVector = if (allVisibleSelected) Icons.Outlined.Deselect else Icons.Outlined.SelectAll,
                        contentDescription = if (allVisibleSelected) {
                            stringResource(R.string.clear_selection)
                        } else {
                            stringResource(R.string.select_all)
                        }
                    )
                }
            }
            IconButton(onClick = {
                navController.navigate(Screen.Archive.route)
            }) {
                Icon(
                    imageVector = Screen.Archive.icon,
                    contentDescription = stringResource(R.string.archive)
                )
            }

            IconButton(onClick = {
                navController.navigate(Screen.Settings.route)
            }) {
                Icon(
                    imageVector = Screen.Settings.icon,
                    contentDescription = stringResource(Screen.Settings.resourceId)
                )
            }
        },
        floatingActionButton = {
            if (selectedPasses.isNotEmpty()) {
                SelectionActions(
                    false,
                    selectedPasses,
                    listState,
                    walletViewModel
                )
            } else {
                FabMenu(
                    items = listOf(
                        FabMenuItem(
                            icon = Icons.Default.MoreHoriz,
                            title = stringResource(R.string.advanced),
                            onClick = {
                                navController.navigate(Screen.AdvancedAdd.route)
                            }
                        ),
                        FabMenuItem(
                            icon = Icons.Default.QrCodeScanner,
                            title = stringResource(R.string.scan_code),
                            onClick = {
                                navController.navigate(Screen.CreateScan.route)
                            }
                        ),
                        FabMenuItem(
                            icon = Icons.Default.Add,
                            title = stringResource(R.string.import_pass),
                            onClick = {
                                launcher.launch(arrayOf(
                                    "application/json+zip",
                                    "application/octet-stream",
                                    "text/json"
                                ).plus(PkpassMimeTypes))
                            }
                        )
                    )
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

        if (loading.value) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
