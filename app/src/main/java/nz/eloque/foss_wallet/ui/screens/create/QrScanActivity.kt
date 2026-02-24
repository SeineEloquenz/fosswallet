package nz.eloque.foss_wallet.ui.screens.create

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import nz.eloque.foss_wallet.R

class QrScanActivity : AppCompatActivity() {
    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var captureManager: CaptureManager

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val pickedUri = uri ?: return@registerForActivityResult
        val result = ImageScanner.scanFrom(contentResolver, pickedUri)
        if (result == null) {
            Toast.makeText(this, getString(R.string.no_barcode_found), Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }

        val scanIntent = Intent().apply {
            putExtra(Intents.Scan.RESULT, result.text)
            putExtra(Intents.Scan.RESULT_FORMAT, result.barcodeFormat.toString())
        }
        setResult(Activity.RESULT_OK, scanIntent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.qr_scan_capture)

        barcodeView = findViewById(R.id.zxing_barcode_scanner)
        captureManager = CaptureManager(this, barcodeView).also {
            it.initializeFromIntent(intent, savedInstanceState)
            it.decode()
        }

        findViewById<ImageButton>(R.id.open_gallery_button).setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    override fun onResume() {
        super.onResume()
        captureManager.onResume()
    }

    override fun onPause() {
        super.onPause()
        captureManager.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        captureManager.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        captureManager.onSaveInstanceState(outState)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        captureManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }
}
