package nz.eloque.foss_wallet.ui.screens.scan

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.google.zxing.BarcodeFormat
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.ui.screens.create.FileScanner
import nz.eloque.foss_wallet.ui.screens.create.ScanActivity

object ScanLauncher {
    @Composable
    fun launch(
        onScanned: (BarCode) -> Unit,
        onCanceled: () -> Unit,
    ): ManagedActivityResultLauncher<Intent, ActivityResult> {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        return rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = { activityResult ->
                if (activityResult.resultCode != Activity.RESULT_OK) {
                    onCanceled()
                    return@rememberLauncherForActivityResult
                }

                val resultData = activityResult.data
                val contents = resultData?.getStringExtra(ScanActivity.EXTRA_RESULT)
                if (contents != null) {
                    val formatName = resultData.getStringExtra(ScanActivity.EXTRA_RESULT_FORMAT)

                    coroutineScope.launch {
                        val result = FileScanner.ScanResult(contents, formatName ?: BarcodeFormat.QR_CODE.name)
                        onScanned(result.toBarCode(context))
                    }
                }
            },
        )
    }
}
