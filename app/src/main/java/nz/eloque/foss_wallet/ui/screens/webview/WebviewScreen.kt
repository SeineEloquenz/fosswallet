package nz.eloque.foss_wallet.ui.screens.webview

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.ui.WalletScaffold
import nz.eloque.foss_wallet.ui.screens.wallet.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebviewScreen(
    url: String,
    onBack: () -> Unit,
    onOpenPass: (passId: String) -> Unit,
    walletViewModel: WalletViewModel = hiltViewModel(),
) {
    WalletScaffold(
        onBack = onBack,
        title = stringResource(id = R.string.webview),
    ) {
        WebviewView(walletViewModel, url, onImported = onBack, onOpenPass = onOpenPass)
    }
}
