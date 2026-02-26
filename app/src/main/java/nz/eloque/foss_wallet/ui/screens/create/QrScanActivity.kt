package nz.eloque.foss_wallet.ui.screens.create

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.CornerRadius
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.ui.theme.WalletTheme

class QrScanActivity : AppCompatActivity() {
    private var barcodeView: DecoratedBarcodeView? = null
    private var captureManager: CaptureManager? = null
    private var hasCameraPermission by mutableStateOf(false)
    private var pendingState: Bundle? = null

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

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
        if (granted) {
            startCameraIfAllowed()
            captureManager?.onResume()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingState = savedInstanceState
        hasCameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val decoratedBarcodeView = DecoratedBarcodeView(this).apply {
            findViewById<View?>(com.google.zxing.client.android.R.id.zxing_viewfinder_view)?.isVisible = false
            findViewById<View?>(com.google.zxing.client.android.R.id.zxing_status_view)?.isVisible = false
        }
        barcodeView = decoratedBarcodeView
        startCameraIfAllowed()
        if (!hasCameraPermission) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        setContent {
            WalletTheme {
                QrScannerContent(
                    barcodeView = decoratedBarcodeView,
                    showCameraPreview = hasCameraPermission,
                    onRequestCameraPermission = {
                        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    onOpenGallery = {
                        pickImageLauncher.launch("image/*")
                    }
                )
            }
        }
    }

    private fun startCameraIfAllowed() {
        if (!hasCameraPermission || captureManager != null) return
        val decoratedBarcodeView = barcodeView ?: return
        try {
            captureManager = SafeCaptureManager(this, decoratedBarcodeView) {
                hasCameraPermission = false
                captureManager = null
            }.also {
                it.setShowMissingCameraPermissionDialog(false)
                it.initializeFromIntent(intent, pendingState)
                pendingState = null
                it.decode()
            }
        } catch (_: SecurityException) {
            hasCameraPermission = false
            captureManager = null
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasCameraPermission) {
            try {
                captureManager?.onResume()
            } catch (_: SecurityException) {
                hasCameraPermission = false
            }
        }
    }

    override fun onPause() {
        if (hasCameraPermission) {
            try {
                captureManager?.onPause()
            } catch (_: SecurityException) {
                hasCameraPermission = false
            }
        }
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        captureManager?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        captureManager?.onSaveInstanceState(outState)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return barcodeView?.onKeyDown(keyCode, event) == true || super.onKeyDown(keyCode, event)
    }
}

private class SafeCaptureManager(
    activity: Activity,
    barcodeView: DecoratedBarcodeView,
    private val onCameraFailure: () -> Unit,
) : CaptureManager(activity, barcodeView) {
    override fun displayFrameworkBugMessageAndExit(message: String?) {
        onCameraFailure()
    }
}

@Composable
private fun QrScannerContent(
    barcodeView: DecoratedBarcodeView,
    showCameraPreview: Boolean,
    onRequestCameraPermission: () -> Unit,
    onOpenGallery: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (showCameraPreview) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { barcodeView }
            )

            ScannerOverlay(modifier = Modifier.fillMaxSize())
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = stringResource(R.string.permission_rationale),
                        color = Color.White
                    )
                    Button(onClick = onRequestCameraPermission) {
                        Text(stringResource(R.string.request_permissions))
                    }
                }
            }
        }

        IconButton(
            onClick = onOpenGallery,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 40.dp)
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White)
        ) {
            Icon(
                imageVector = Icons.Filled.Image,
                contentDescription = stringResource(R.string.choose_image),
                tint = Color.Black,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun ScannerOverlay(modifier: Modifier = Modifier) {
    val cornerRadius = with(LocalDensity.current) { 24.dp.toPx() }
    val strokeWidth = with(LocalDensity.current) { 2.dp.toPx() }

    Canvas(
        modifier = modifier.graphicsLayer {
            compositingStrategy = CompositingStrategy.Offscreen
        }
    ) {
        val frameWidth = size.width * 0.72f
        val frameHeight = frameWidth
        val left = (size.width - frameWidth) / 2f
        val top = (size.height - frameHeight) / 2f

        drawRect(color = Color.Black.copy(alpha = 0.45f))
        drawRoundRect(
            color = Color.Transparent,
            topLeft = androidx.compose.ui.geometry.Offset(left, top),
            size = androidx.compose.ui.geometry.Size(frameWidth, frameHeight),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            blendMode = BlendMode.Clear,
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.95f),
            topLeft = androidx.compose.ui.geometry.Offset(left, top),
            size = androidx.compose.ui.geometry.Size(frameWidth, frameHeight),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(width = strokeWidth),
        )
    }
}
