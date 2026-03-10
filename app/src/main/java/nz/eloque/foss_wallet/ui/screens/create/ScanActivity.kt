package nz.eloque.foss_wallet.ui.screens.create

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.ui.theme.WalletTheme
import java.util.concurrent.Executors
import zxingcpp.BarcodeReader

class ScanActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_RESULT = "scan_result"
        const val EXTRA_RESULT_FORMAT = "scan_result_format"
    }

    private var previewView: PreviewView? = null
    private var cameraPermissionState by mutableStateOf(CameraPermissionState.Requesting)
    private var isTorchEnabled by mutableStateOf(false)
    private var lensFacing by mutableStateOf(CameraSelector.LENS_FACING_BACK)
    private var hasDeliveredResult = false
    private var cameraProvider: ProcessCameraProvider? = null
    private var boundCamera: Camera? = null
    private var isCameraBound = false
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val barcodeReader = BarcodeReader(
        BarcodeReader.Options(
            tryHarder = true,
            tryRotate = true,
            tryInvert = true,
        )
    )

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val pickedUri = uri ?: return@registerForActivityResult
        val result = ImageScanner.scanFrom(contentResolver, pickedUri)
        if (result == null) {
            Toast.makeText(this, getString(R.string.no_barcode_found), Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }

        deliverScanResult(result.text, result.format)
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        cameraPermissionState = if (granted) CameraPermissionState.Granted else CameraPermissionState.Denied
        if (granted) {
            startCameraIfAllowed()
        }
    }

    private fun requestCameraPermission() {
        cameraPermissionState = CameraPermissionState.Requesting
        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun onCameraAccessFailed() {
        cameraPermissionState = CameraPermissionState.Denied
        cameraProvider?.unbindAll()
        isCameraBound = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val hasCameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        cameraPermissionState = if (hasCameraPermission) CameraPermissionState.Granted else CameraPermissionState.Requesting
        setContent {
            WalletTheme {
                QrScannerContent(
                    permissionState = cameraPermissionState,
                    isTorchEnabled = isTorchEnabled,
                    onPreviewReady = { view ->
                        previewView = view
                        if (cameraPermissionState == CameraPermissionState.Granted) {
                            startCameraIfAllowed()
                        }
                    },
                    onRequestCameraPermission = ::requestCameraPermission,
                    onOpenGallery = {
                        pickImageLauncher.launch("image/*")
                    },
                    onToggleFlashlight = ::toggleFlashlight,
                    onSwitchCamera = ::switchCameraLens,
                )
            }
        }

        if (!hasCameraPermission) {
            requestCameraPermission()
        }
    }

    private fun startCameraIfAllowed() {
        if (cameraPermissionState != CameraPermissionState.Granted || hasDeliveredResult || isCameraBound) return
        val boundPreviewView = previewView ?: return

        val future = ProcessCameraProvider.getInstance(this)
        future.addListener(
            {
                try {
                    val provider = future.get()
                    cameraProvider = provider
                    bindCameraUseCases(provider, boundPreviewView)
                } catch (_: SecurityException) {
                    onCameraAccessFailed()
                }
            },
            ContextCompat.getMainExecutor(this)
        )
    }

    private fun bindCameraUseCases(provider: ProcessCameraProvider, boundPreviewView: PreviewView) {
        provider.unbindAll()
        boundCamera = null
        isCameraBound = false

        val preview = Preview.Builder().build().also {
            it.surfaceProvider = boundPreviewView.surfaceProvider
        }

        val analyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        analyzer.setAnalyzer(cameraExecutor) { image ->
            if (hasDeliveredResult) {
                image.close()
                return@setAnalyzer
            }

            val result = image.use { barcodeReader.read(it).firstOrNull() }
            val text = result?.text?.takeIf { it.isNotBlank() } ?: return@setAnalyzer
            val format = result.format.name

            hasDeliveredResult = true
            runOnUiThread {
                deliverScanResult(text, format)
            }
        }

        val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        try {
            val camera = provider.bindToLifecycle(this, selector, preview, analyzer)
            boundCamera = camera
            camera.cameraControl.enableTorch(isTorchEnabled)
            isCameraBound = true
        } catch (_: SecurityException) {
            onCameraAccessFailed()
        } catch (_: IllegalArgumentException) {
            lensFacing = CameraSelector.LENS_FACING_BACK
            isTorchEnabled = false
            onCameraAccessFailed()
        }
    }

    private fun toggleFlashlight() {
        if (cameraPermissionState != CameraPermissionState.Granted) return
        val camera = boundCamera ?: return
        if (!camera.cameraInfo.hasFlashUnit()) return

        isTorchEnabled = !isTorchEnabled
        camera.cameraControl.enableTorch(isTorchEnabled)
    }

    private fun switchCameraLens() {
        if (cameraPermissionState != CameraPermissionState.Granted) return

        val provider = cameraProvider ?: return
        val nextLens = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }

        val nextSelector = CameraSelector.Builder().requireLensFacing(nextLens).build()
        if (!provider.hasCamera(nextSelector)) return

        lensFacing = nextLens
        isTorchEnabled = false
        previewView?.let { bindCameraUseCases(provider, it) }
    }

    override fun onResume() {
        super.onResume()
        startCameraIfAllowed()
    }

    override fun onPause() {
        cameraProvider?.unbindAll()
        isCameraBound = false
        super.onPause()
    }

    override fun onDestroy() {
        cameraProvider?.unbindAll()
        isCameraBound = false
        cameraExecutor.shutdownNow()
        super.onDestroy()
    }

    private fun deliverScanResult(text: String, format: String) {
        if (isFinishing || isDestroyed) return

        val scanIntent = Intent().apply {
            putExtra(EXTRA_RESULT, text)
            putExtra(EXTRA_RESULT_FORMAT, format)
        }
        setResult(Activity.RESULT_OK, scanIntent)
        finish()
    }
}

private enum class CameraPermissionState {
    Requesting,
    Granted,
    Denied,
}

@Composable
private fun QrScannerContent(
    permissionState: CameraPermissionState,
    isTorchEnabled: Boolean,
    onPreviewReady: (PreviewView) -> Unit,
    onRequestCameraPermission: () -> Unit,
    onOpenGallery: () -> Unit,
    onToggleFlashlight: () -> Unit,
    onSwitchCamera: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (permissionState) {
            CameraPermissionState.Granted -> {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        PreviewView(context).apply {
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }.also(onPreviewReady)
                    }
                )

                ScannerOverlay(modifier = Modifier.fillMaxSize())
            }
            CameraPermissionState.Requesting,
            CameraPermissionState.Denied -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    if (permissionState == CameraPermissionState.Denied) {
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
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 40.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            IconButton(
                onClick = onToggleFlashlight,
                enabled = permissionState == CameraPermissionState.Granted,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(
                    imageVector = if (isTorchEnabled) Icons.Filled.FlashOff else Icons.Filled.FlashOn,
                    contentDescription = if (isTorchEnabled) "Turn flashlight off" else "Turn flashlight on",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(
                onClick = onOpenGallery,
                modifier = Modifier
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

            IconButton(
                onClick = onSwitchCamera,
                enabled = permissionState == CameraPermissionState.Granted,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(
                    imageVector = Icons.Filled.Cameraswitch,
                    contentDescription = "Switch camera",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
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
