package nz.eloque.foss_wallet

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nz.eloque.foss_wallet.shortcut.ShortcutService
import nz.eloque.foss_wallet.ui.ImportEventsEffect
import nz.eloque.foss_wallet.ui.Route
import nz.eloque.foss_wallet.ui.WalletApp
import nz.eloque.foss_wallet.ui.screens.create.scanSource
import nz.eloque.foss_wallet.ui.screens.scan.FilePreviewContract
import nz.eloque.foss_wallet.ui.screens.scan.FilePreviewInput
import nz.eloque.foss_wallet.ui.screens.wallet.WalletViewModel
import nz.eloque.foss_wallet.ui.theme.WalletTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val walletViewModel: WalletViewModel by viewModels()
    private var incomingFile by mutableStateOf<IncomingFile?>(null)
    private var importRequestId by mutableIntStateOf(0)

    private data class IncomingFile(
        val uri: Uri,
        val mimeType: String?,
    )

    private fun fileFrom(intent: Intent): IncomingFile? {
        val uri =
            when (intent.action) {
                Intent.ACTION_VIEW -> intent.data
                Intent.ACTION_SEND -> intent.sharedFileUri()
                else -> null
            }
        return uri?.let { IncomingFile(it, intent.type) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        incomingFile = fileFrom(intent)
        importRequestId++
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("import_request_id", importRequestId)
        super.onSaveInstanceState(outState)
    }

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        importRequestId = savedInstanceState?.getInt("import_request_id") ?: 0
        incomingFile = fileFrom(intent)

        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val coroutineScope = rememberCoroutineScope()
            ImportEventsEffect(walletViewModel, navController)
            val previewLauncher =
                rememberLauncherForActivityResult(FilePreviewContract()) { barcode ->
                    if (barcode != null) navController.navigate(Route.ScanResult(barcode))
                }
            val file = incomingFile
            var importHandled by rememberSaveable(importRequestId) { mutableStateOf(false) }
            LaunchedEffect(importRequestId, file) {
                if (importHandled) return@LaunchedEffect
                importHandled = true
                val source =
                    if (file != null && ShortcutService.SCHEME != file.uri.scheme) {
                        withContext(Dispatchers.IO) { contentResolver.scanSource(file.uri, file.mimeType) }
                    } else {
                        null
                    }
                if (source != null) {
                    previewLauncher.launch(FilePreviewInput(file!!.uri, file.mimeType))
                    return@LaunchedEffect
                }

                if (file != null && ShortcutService.SCHEME != file.uri.scheme) {
                    walletViewModel.import(listOf(file.uri))
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
                LaunchedEffect("Permissions") {
                    coroutineScope.launch(Dispatchers.IO) { permissionState.launchPermissionRequest() }
                }
            }
            WalletTheme {
                Box(
                    modifier =
                        androidx.compose.ui.Modifier
                            .fillMaxSize(),
                ) {
                    WalletApp(
                        navController,
                    )
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun Intent.sharedFileUri(): Uri? {
        if (action != Intent.ACTION_SEND) return null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)?.let { return it }
        } else {
            getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { return it }
        }

        return clipData?.let { clip ->
            (0 until clip.itemCount).firstNotNullOfOrNull { clip.getItemAt(it).uri }
        }
    }
}
