package nz.eloque.foss_wallet.ui.screens.scan

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.ui.WalletScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    scanViewModel: ScanViewModel,
    onBack: () -> Unit,
    onScanned: (BarCode) -> Unit,
    onCancelled: () -> Unit,
    onPassCreated: (passId: String) -> Unit,
    onOpenUrl: (url: String) -> Unit,
    onManualEntry: (BarCode) -> Unit,
    initialBarcode: BarCode? = null,
) {
    WalletScaffold(
        onBack = onBack,
        title = stringResource(id = R.string.create_pass),
    ) {
        ScanView(
            scanViewModel = scanViewModel,
            onScanned = onScanned,
            onCancelled = onCancelled,
            onPassCreated = onPassCreated,
            onOpenUrl = onOpenUrl,
            onManualEntry = onManualEntry,
            initialBarcode = initialBarcode,
        )
    }
}
