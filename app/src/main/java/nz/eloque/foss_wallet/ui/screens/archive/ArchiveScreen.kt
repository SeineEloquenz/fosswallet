package nz.eloque.foss_wallet.ui.screens.archive

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.LocalizedPassWithTags
import nz.eloque.foss_wallet.ui.WalletScaffold
import nz.eloque.foss_wallet.ui.Screen
import nz.eloque.foss_wallet.ui.screens.wallet.SelectionActions
import nz.eloque.foss_wallet.ui.screens.wallet.WalletView
import nz.eloque.foss_wallet.ui.screens.wallet.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    navController: NavHostController,
    walletViewModel: WalletViewModel,
) {
    val listState = rememberLazyListState()
    val selectedPasses = remember { mutableStateSetOf<LocalizedPassWithTags>() }

    WalletScaffold(
        navController = navController,
        title = stringResource(id = R.string.archive),
        toolWindow = true,
        actions = {
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
                    true,
                    selectedPasses,
                    listState,
                    walletViewModel
                )
            }
        },
    ) { scrollBehavior ->
        WalletView(
            navController = navController,
            walletViewModel = walletViewModel,
            archive = true,
            emptyIcon = Icons.Default.Archive,
            listState = listState,
            scrollBehavior = scrollBehavior,
            selectedPasses = selectedPasses
        )
    }
}
