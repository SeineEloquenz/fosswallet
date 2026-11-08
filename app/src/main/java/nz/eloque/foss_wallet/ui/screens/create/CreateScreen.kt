package nz.eloque.foss_wallet.ui.screens.create

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.ui.WalletScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    initialBarcode: BarCode?,
    onBack: () -> Unit,
    onScanCancelled: () -> Unit,
    onPassCreated: (passId: String) -> Unit,
    createViewModel: CreateViewModel =
        hiltViewModel<CreateViewModel, CreateViewModel.Factory>(creationCallback = { it.create(initialBarcode) }),
) {
    WalletScaffold(
        onBack = onBack,
        title = stringResource(id = R.string.create_pass),
    ) {
        CreateView(createViewModel, onScanCancelled, onPassCreated)
    }
}
