package nz.eloque.foss_wallet.ui.screens.create

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import nz.eloque.foss_wallet.ui.screens.create.FileScanner.toScanResult
import nz.eloque.foss_wallet.ui.screens.scan.ScanContract
import nz.eloque.foss_wallet.ui.theme.WalletTheme
import zxingcpp.BarcodeReader
import java.io.File
import java.util.concurrent.Executors

class ScanActivity : AppCompatActivity() {
    private var previewView: PreviewView? = null
    private var cameraPermissionState by mutableStateOf(CameraPermissionState.Requesting)
    private var isTorchEnabled by mutableStateOf(false)
    private var lensFacing by mutableIntStateOf(CameraSelector.LENS_FACING_BACK)
    private var hasDeliveredResult = false
    private var cameraProvider: ProcessCameraProvider? = null
    private var boundCamera: Camera? = null
    private var isCameraBound = false
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val barcodeReader = BarcodeReader(BarcodeReader.Options(tryHarder = true, tryRotate = true, tryInvert = true))
    private var imageCapture: ImageCapture? = null
    private var previewMimeType by mutableStateOf<String?>(null)
    private var previewUri by mutableStateOf<Uri?>(null)
    private var isChoosingFile by mutableStateOf(false)
    private var isActive by mutableStateOf(false)
    private var isCapturing by mutableStateOf(false)
    private val capturedFiles = mutableListOf<File>()

    private val pickFileLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            isChoosingFile = false
            if (uri != null) showPreview(uri)
        }

    private fun pickFile() {
        isChoosingFile = true
        pickFileLauncher.launch(arrayOf("image/*", "application/pdf"))
    }

    private fun showPreview(uri: Uri) {
        previewMimeType = contentResolver.getType(uri)
        previewUri = uri
        cameraProvider?.unbindAll()
        isCameraBound = false
        isTorchEnabled = false
    }

    private fun takePhoto() {
        val capture = imageCapture ?: return
        if (isCapturing) return
        isCapturing = true
        capture.targetRotation = previewView!!.display.rotation
        val file = File.createTempFile("barcode-", ".jpg", cacheDir)
        capturedFiles.add(file)
        capture.takePicture(
            ImageCapture.OutputFileOptions.Builder(file).build(),
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    isCapturing = false
                    showPreview(Uri.fromFile(file))
                }

                override fun onError(exception: ImageCaptureException) {
                    isCapturing = false
                    Toast.makeText(this@ScanActivity, R.string.photo_capture_failed, Toast.LENGTH_SHORT).show()
                }
            },
        )
    }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
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

        savedInstanceState?.getStringArrayList("captured_files")?.forEach { capturedFiles.add(File(it)) }
        previewMimeType = savedInstanceState?.getString("preview_mime") ?: intent.type
        previewUri = savedInstanceState?.getString("preview_uri")?.let(Uri::parse) ?: intent.data

        val hasCameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        cameraPermissionState = if (hasCameraPermission) CameraPermissionState.Granted else CameraPermissionState.Requesting
        setContent {
            WalletTheme {
                val uri = previewUri
                if (uri != null) {
                    val goBack = {
                        if (intent.data != null) {
                            finish()
                        } else {
                            previewUri = null
                            previewView = null
                            if (cameraPermissionState != CameraPermissionState.Granted) requestCameraPermission()
                        }
                    }
                    BackHandler(onBack = goBack)
                    ImagePreview(
                        uri = uri,
                        mimeType = previewMimeType,
                        scanningEnabled = isActive && !isChoosingFile,
                        onPickFile = ::pickFile,
                        onScanned = ::deliverScanResult,
                    )
                } else {
                    QrScannerContent(
                        permissionState = cameraPermissionState,
                        onPickFile = ::pickFile,
                        isTorchEnabled = isTorchEnabled,
                        onPreviewReady = { view ->
                            previewView = view
                            if (cameraPermissionState == CameraPermissionState.Granted) {
                                startCameraIfAllowed()
                            }
                        },
                        onRequestCameraPermission = ::requestCameraPermission,
                        onTakePhoto = ::takePhoto,
                        isCapturing = isCapturing,
                        onToggleFlashlight = ::toggleFlashlight,
                        onSwitchCamera = ::switchCameraLens,
                    )
                }
            }
        }

        if (!hasCameraPermission && previewUri == null) {
            requestCameraPermission()
        }
    }

    private fun startCameraIfAllowed() {
        if (cameraPermissionState != CameraPermissionState.Granted || hasDeliveredResult || isCameraBound || previewUri != null) return
        val boundPreviewView = previewView ?: return

        val future = ProcessCameraProvider.getInstance(this)
        future.addListener(
            {
                try {
                    val provider = future.get()
                    cameraProvider = provider
                    if (isActive && previewUri == null && previewView === boundPreviewView && !hasDeliveredResult) {
                        bindCameraUseCases(provider, boundPreviewView)
                    }
                } catch (_: SecurityException) {
                    onCameraAccessFailed()
                }
            },
            ContextCompat.getMainExecutor(this),
        )
    }

    private fun bindCameraUseCases(
        provider: ProcessCameraProvider,
        boundPreviewView: PreviewView,
    ) {
        provider.unbindAll()
        boundCamera = null
        isCameraBound = false

        val preview =
            Preview.Builder().build().also {
                it.surfaceProvider = boundPreviewView.surfaceProvider
            }

        val capture = ImageCapture.Builder().build()
        imageCapture = capture

        val analyzer =
            ImageAnalysis
                .Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
        analyzer.setAnalyzer(cameraExecutor) { image ->
            val result =
                try {
                    image.use { barcodeReader.read(it).firstNotNullOfOrNull { result -> result.toScanResult() } }
                } catch (_: RuntimeException) {
                    null
                }
            if (result != null) {
                runOnUiThread {
                    if (isActive && !isChoosingFile && !isCapturing && previewUri == null) deliverScanResult(result)
                }
            }
        }

        val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        try {
            val camera = provider.bindToLifecycle(this, selector, preview, capture, analyzer)
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
        val nextLens =
            if (lensFacing == CameraSelector.LENS_FACING_BACK) {
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
        isActive = true
        startCameraIfAllowed()
    }

    override fun onPause() {
        isActive = false
        cameraProvider?.unbindAll()
        isCameraBound = false
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("preview_uri", previewUri?.toString())
        outState.putString("preview_mime", previewMimeType)
        outState.putStringArrayList("captured_files", ArrayList(capturedFiles.map { it.path }))
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        cameraProvider?.unbindAll()
        isCameraBound = false
        cameraExecutor.shutdown()
        if (!isChangingConfigurations) capturedFiles.forEach { it.delete() }
        super.onDestroy()
    }

    private fun deliverScanResult(result: FileScanner.ScanResult) {
        if (isFinishing || isDestroyed || hasDeliveredResult) return
        hasDeliveredResult = true
        setResult(Activity.RESULT_OK, ScanContract.resultIntent(result.toBarCode()))
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
    onPickFile: () -> Unit,
    isTorchEnabled: Boolean,
    onPreviewReady: (PreviewView) -> Unit,
    onRequestCameraPermission: () -> Unit,
    onTakePhoto: () -> Unit,
    isCapturing: Boolean,
    onToggleFlashlight: () -> Unit,
    onSwitchCamera: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (permissionState) {
            CameraPermissionState.Granted -> {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        PreviewView(context)
                            .apply {
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                            }.also(onPreviewReady)
                    },
                )

                ScannerOverlay(modifier = Modifier.fillMaxSize())
            }

            CameraPermissionState.Requesting,
            CameraPermissionState.Denied,
            -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                ) {
                    if (permissionState == CameraPermissionState.Denied) {
                        Column(
                            modifier =
                                Modifier
                                    .align(Alignment.Center)
                                    .padding(horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(48.dp),
                            )
                            Text(
                                text = stringResource(R.string.permission_rationale),
                                color = Color.White,
                            )
                            Button(onClick = onRequestCameraPermission) {
                                Text(stringResource(R.string.request_permissions))
                            }
                        }
                    }
                }
            }
        }

        FilePickerButton(
            onClick = onPickFile,
            modifier = Modifier.align(Alignment.BottomEnd).safeDrawingPadding().padding(16.dp),
        )

        Row(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 40.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            IconButton(
                onClick = onToggleFlashlight,
                enabled = permissionState == CameraPermissionState.Granted,
                modifier =
                    Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White),
            ) {
                Icon(
                    imageVector = if (isTorchEnabled) Icons.Filled.FlashOff else Icons.Filled.FlashOn,
                    contentDescription = if (isTorchEnabled) "Turn flashlight off" else "Turn flashlight on",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp),
                )
            }

            IconButton(
                onClick = onTakePhoto,
                enabled = permissionState == CameraPermissionState.Granted && !isCapturing,
                modifier =
                    Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White),
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = stringResource(R.string.take_photo),
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp),
                )
            }

            IconButton(
                onClick = onSwitchCamera,
                enabled = permissionState == CameraPermissionState.Granted,
                modifier =
                    Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White),
            ) {
                Icon(
                    imageVector = Icons.Filled.Cameraswitch,
                    contentDescription = "Switch camera",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
internal fun ScannerOverlay(modifier: Modifier = Modifier) {
    val cornerRadius = with(LocalDensity.current) { 24.dp.toPx() }
    val strokeWidth = with(LocalDensity.current) { 2.dp.toPx() }

    Canvas(
        modifier =
            modifier.graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            },
    ) {
        val frameWidth = size.width * 0.72f
        val frameHeight = frameWidth
        val left = (size.width - frameWidth) / 2f
        val top = (size.height - frameHeight) / 2f

        drawRect(color = Color.Black.copy(alpha = 0.45f))
        drawRoundRect(
            color = Color.Transparent,
            topLeft =
                androidx.compose.ui.geometry
                    .Offset(left, top),
            size =
                androidx.compose.ui.geometry
                    .Size(frameWidth, frameHeight),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            blendMode = BlendMode.Clear,
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.95f),
            topLeft =
                androidx.compose.ui.geometry
                    .Offset(left, top),
            size =
                androidx.compose.ui.geometry
                    .Size(frameWidth, frameHeight),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(width = strokeWidth),
        )
    }
}
