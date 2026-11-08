package nz.eloque.foss_wallet.ui.screens.scan

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.nielstron.bcbp.IataBcbp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.persistence.BarcodePosition
import nz.eloque.foss_wallet.ui.screens.pass.Barcodes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanView(
    scanViewModel: ScanViewModel,
    onScanned: (BarCode) -> Unit,
    onCancelled: () -> Unit,
    onPassCreated: (passId: String) -> Unit,
    onOpenUrl: (url: String) -> Unit,
    onManualEntry: (BarCode) -> Unit,
    initialBarcode: BarCode? = null,
) {
    var initialScanHandled by rememberSaveable { mutableStateOf(initialBarcode != null) }

    val scanLauncher =
        rememberLauncherForActivityResult(ScanContract()) { barcode ->
            if (barcode != null) onScanned(barcode) else onCancelled()
        }

    LaunchedEffect(initialScanHandled) {
        if (!initialScanHandled) {
            initialScanHandled = true
            scanLauncher.launch(Unit)
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
    ) {
        initialBarcode?.let {
            Barcodes(
                barcodes = listOf(it),
                barcodePosition = BarcodePosition.Center,
                increaseFullscreenBrightness = false,
            )

            val bcbp = IataBcbp.parse(it.message)
            if (bcbp != null) {
                val coroutineScope = rememberCoroutineScope()
                TextButton(
                    onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            val passId = scanViewModel.saveBcbpPass(it, bcbp)
                            withContext(Dispatchers.Main) { onPassCreated(passId) }
                        }
                    },
                ) {
                    Text(stringResource(R.string.create_boarding_pass))
                }
            }
            if (it.message.startsWith("https://") || it.message.startsWith("http://")) {
                TextButton(onClick = { onOpenUrl(it.message) }) {
                    Text(stringResource(R.string.webview))
                }
            }

            TextButton(onClick = { onManualEntry(it) }) {
                Text(stringResource(R.string.manual_entry))
            }
        }
    }
}
