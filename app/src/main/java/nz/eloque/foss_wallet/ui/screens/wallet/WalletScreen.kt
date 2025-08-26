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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.persistence.loader.Loader
import nz.eloque.foss_wallet.ui.components
import nz.eloque.foss_wallet.ui.Screen
import nz.eloque.foss_wallet.ui.WalletScaffold
import nz.eloque.foss_wallet.utils.isScrollingUp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    navController: NavHostController,
    passViewModel: PassViewModel,
) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val coroutineScope = rememberCoroutineScope()

    val listState = rememberLazyListState()

    val loading = remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { res ->
        res?.let {
            println("selected file URI $res")
            coroutineScope.launch {
                loading.value = true
                withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(res)?.use {
                        Loader(context).handleInputStream(
                            it,
                            navController,
                            passViewModel,
                            coroutineScope
                        )
                    }
                }
                loading.value = false
            }
        }
    }
    val selectedPasses = remember { mutableStateSetOf<Pass>() }

    WalletScaffold(
        navController = navController,
        title = stringResource(id = Screen.Wallet.resourceId),
        actions = {
            VisibilityToggleButton(uiState.isAuthenticated) {
                uiState.isAuthenticated ? conceal() : reveal()
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
                    selectedPasses,
                    listState,
                    passViewModel
                )
            } else {
                ExtendedFloatingActionButton(
                    text = { Text(stringResource(R.string.add_pass)) },
                    icon = { Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.add_pass)) },
                    expanded = listState.isScrollingUp(),
                    onClick = { launcher.launch(arrayOf("*/*")) }
                )
            }
        },
    ) { scrollBehavior ->
        WalletView(navController, passViewModel, listState = listState, scrollBehavior = scrollBehavior, selectedPasses = selectedPasses)

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
