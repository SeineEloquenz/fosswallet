package nz.eloque.foss_wallet.ui.screens.scan

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import com.google.zxing.BarcodeFormat
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.ui.screens.create.ScanActivity

object ScanLauncher {
    @Composable
    fun launch(onScanned: (BarCode) -> Unit): ManagedActivityResultLauncher<Intent, ActivityResult> {
        val context = LocalContext.current
        val resources = LocalResources.current

        return rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = { activityResult ->
                if (activityResult.resultCode == Activity.RESULT_OK) {
                    val resultData = activityResult.data
                    val contents = resultData?.getStringExtra(ScanActivity.EXTRA_RESULT)
                    if (contents != null) {
                        val formatName = resultData.getStringExtra(ScanActivity.EXTRA_RESULT_FORMAT)

                        val scannedFormat =
                            try {
                                BarcodeFormat.valueOf(formatName ?: BarcodeFormat.QR_CODE.name)
                            } catch (_: IllegalArgumentException) {
                                Toast
                                    .makeText(
                                        context,
                                        resources.getString(R.string.no_barcode_format_given),
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                BarcodeFormat.QR_CODE
                            }
                        val barcode =
                            BarCode(
                                message = contents,
                                altText = contents,
                                format = scannedFormat,
                                encoding = Charsets.UTF_8,
                            )
                        onScanned(barcode)
                    }
                }
            },
        )
    }
}
