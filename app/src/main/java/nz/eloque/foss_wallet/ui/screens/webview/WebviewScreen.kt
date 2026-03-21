package nz.eloque.foss_wallet.ui.screens.webview

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.ui.WalletScaffold
import nz.eloque.foss_wallet.ui.screens.wallet.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebviewScreen(
    navController: NavHostController,
    walletViewModel: WalletViewModel,
    url: String,
) {
    WalletScaffold(
        navController = navController,
        toolWindow = true,
        title = stringResource(id = R.string.webview)
    ) {
        WebviewView(navController, walletViewModel, url)
    }
}