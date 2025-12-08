package nz.eloque.foss_wallet.ui.screens.webview

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.ui.WalletScaffold
import nz.eloque.foss_wallet.ui.screens.webview.WebviewView
import nz.eloque.foss_wallet.ui.screens.wallet.PassViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebviewScreen(
    navController: NavHostController,
    passViewModel: PassViewModel,
    url: String,
) {
    WalletScaffold(
        navController = navController,
        toolWindow = true,
        title = stringResource(id = R.string.webview)
    ) {
        WebviewView(navController, passViewModel, url)
    }
}