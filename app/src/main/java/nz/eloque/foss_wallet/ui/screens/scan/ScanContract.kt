package nz.eloque.foss_wallet.ui.screens.scan

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.google.zxing.BarcodeFormat
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.ui.screens.create.ScanActivity
import java.nio.charset.Charset

/** Starts [ScanActivity] and returns the scanned barcode, or null if scanning was cancelled. */
class ScanContract : ActivityResultContract<Unit, BarCode?>() {
    override fun createIntent(
        context: Context,
        input: Unit,
    ): Intent = Intent(context, ScanActivity::class.java)

    override fun parseResult(
        resultCode: Int,
        intent: Intent?,
    ): BarCode? = intent?.takeIf { resultCode == Activity.RESULT_OK }?.toBarCode()

    companion object {
        private const val EXTRA_MESSAGE = "scan_result"
        private const val EXTRA_FORMAT = "scan_result_format"
        private const val EXTRA_ENCODING = "scan_result_encoding"
        private const val EXTRA_ALT_TEXT = "scan_result_alt_text"

        fun resultIntent(barcode: BarCode): Intent =
            Intent()
                .putExtra(EXTRA_MESSAGE, barcode.message)
                .putExtra(EXTRA_FORMAT, barcode.format.name)
                .putExtra(EXTRA_ENCODING, barcode.encoding.name())
                .putExtra(EXTRA_ALT_TEXT, barcode.altText)

        private fun Intent.toBarCode(): BarCode? {
            val message = getStringExtra(EXTRA_MESSAGE) ?: return null
            val format = BarcodeFormat.entries.firstOrNull { it.name == getStringExtra(EXTRA_FORMAT) } ?: return null
            val encoding = getStringExtra(EXTRA_ENCODING)?.let { Charset.forName(it) } ?: return null
            return BarCode(format, message, encoding, getStringExtra(EXTRA_ALT_TEXT))
        }
    }
}
