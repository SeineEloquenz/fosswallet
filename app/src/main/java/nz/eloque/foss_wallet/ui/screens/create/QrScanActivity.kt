package nz.eloque.foss_wallet.ui.screens.create

import android.app.Activity
import android.content.Intent
import android.widget.Button
import android.widget.Toast
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import nz.eloque.foss_wallet.R

class QrScanActivity : CaptureActivity() {
    override fun initializeContent(): DecoratedBarcodeView {
        setContentView(R.layout.qr_scan_capture)
        findViewById<Button>(R.id.open_gallery_button).setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            @Suppress("DEPRECATION")
            startActivityForResult(intent, REQUEST_PICK_IMAGE)
        }
        return findViewById(R.id.zxing_barcode_scanner)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_PICK_IMAGE || resultCode != Activity.RESULT_OK) {
            return
        }

        val uri = data?.data ?: return
        val result = ImageScanner.scanFrom(contentResolver, uri)
        if (result == null) {
            Toast.makeText(this, getString(R.string.no_barcode_found), Toast.LENGTH_SHORT).show()
            return
        }

        val scanIntent = Intent().apply {
            putExtra(Intents.Scan.RESULT, result.text)
            putExtra(Intents.Scan.RESULT_FORMAT, result.barcodeFormat.toString())
        }
        setResult(Activity.RESULT_OK, scanIntent)
        finish()
    }

    private companion object {
        const val REQUEST_PICK_IMAGE = 1001
    }
}
