package nz.eloque.foss_wallet.ui.screens.scan

import android.content.Intent
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import de.nielstron.bcbp.IataBcbp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.persistence.BarcodePosition
import nz.eloque.foss_wallet.ui.Screen
import nz.eloque.foss_wallet.ui.screens.create.ScanActivity
import nz.eloque.foss_wallet.ui.screens.pass.BarcodesView
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanView(
    navController: NavHostController,
    scanViewModel: ScanViewModel,
) {
    val context = LocalContext.current

    var scannedBarcode by remember { mutableStateOf<BarCode?>(null) }
    var initialScanHandled by remember { mutableStateOf(false) }

    val scanLauncher =
        ScanLauncher.launch {
            scannedBarcode = it
        }

    LaunchedEffect(initialScanHandled) {
        if (!initialScanHandled) {
            initialScanHandled = true
            scanLauncher.launch(
                Intent(context, ScanActivity::class.java),
            )
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
        scannedBarcode?.let {
            BarcodesView(
                legacyRendering = false,
                barcodes = listOf(it),
                barcodePosition = BarcodePosition.Center,
                increaseBrightness = false,
            )

            TextButton(
                onClick = {
                    Screen.Create.navigate(navController, scannedBarcode!!)
                },
            ) {
                Text(stringResource(R.string.manual_entry))
            }

            val bcbp = IataBcbp.parse(it.message)
            if (bcbp != null) {
                val coroutineScope = rememberCoroutineScope()
                TextButton(
                    onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            val passId = scanViewModel.saveBcbpPass(it, bcbp)
                            withContext(Dispatchers.Main) {
                                navController.navigate("pass/$passId") {
                                    popUpTo(Screen.Scan.route) {
                                        inclusive = true
                                    }
                                }
                            }
                        }
                    },
                ) {
                    Text(stringResource(R.string.create_boarding_pass))
                }
            }
            if (it.message.startsWith("https://") || it.message.startsWith("http://")) {
                TextButton(
                    onClick = {
                        val url = URLEncoder.encode(it.message, Charsets.UTF_8.name())
                        navController.navigate("${Screen.Web.route}/$url")
                    },
                ) {
                    Text(stringResource(R.string.webview))
                }
            }
        }
    }
}
