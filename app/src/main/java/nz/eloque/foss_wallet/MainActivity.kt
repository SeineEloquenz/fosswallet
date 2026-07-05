package nz.eloque.foss_wallet

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nz.eloque.foss_wallet.persistence.SettingsStore
import nz.eloque.foss_wallet.persistence.loader.Loader
import nz.eloque.foss_wallet.persistence.loader.LoaderResult
import nz.eloque.foss_wallet.shortcut.Shortcut
import nz.eloque.foss_wallet.ui.Screen
import nz.eloque.foss_wallet.ui.WalletApp
import nz.eloque.foss_wallet.ui.screens.create.FileScanner
import nz.eloque.foss_wallet.ui.screens.create.ScanSource
import nz.eloque.foss_wallet.ui.screens.wallet.WalletViewModel
import nz.eloque.foss_wallet.ui.theme.WalletTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val walletViewModel: WalletViewModel by viewModels()

    @Inject
    lateinit var settingsStore: SettingsStore

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shareSource: ScanSource? =
            if (intent.action == Intent.ACTION_SEND) {
                if (intent.type?.startsWith("image/") == true) {
                    ScanSource.Image
                } else if (intent.type == "application/pdf") {
                    ScanSource.Pdf
                } else {
                    null
                }
            } else {
                null
            }
        val dataUri =
            when {
                Intent.ACTION_VIEW == intent.action -> {
                    intent.data
                }

                shareSource != null -> {
                    intent.sharedFileUri()
                }

                Intent.ACTION_SEND == intent.action -> {
                    val count =
                        intent.clipData
                            ?.itemCount
                            ?.minus(1)
                            ?.coerceAtLeast(0)
                    count?.let { intent.clipData?.getItemAt(it)?.uri }
                }

                else -> {
                    null
                }
            }

        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val coroutineScope = rememberCoroutineScope()
            var isProcessingFileShare by remember { mutableStateOf(false) }
            LaunchedEffect(dataUri, shareSource != null) {
                if (shareSource != null && dataUri != null) {
                    isProcessingFileShare = true
                    coroutineScope.launch(Dispatchers.IO) {
                        val barcode =
                            runCatching { FileScanner.scanFrom(contentResolver, dataUri, shareSource)?.toBarCode() }.getOrNull()
                        withContext(Dispatchers.Main) {
                            isProcessingFileShare = false
                            if (barcode != null) {
                                Screen.Create.navigate(navController, barcode)
                            } else {
                                Toast.makeText(this@MainActivity, getString(R.string.no_barcode_found), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    return@LaunchedEffect
                }

                if (Shortcut.SCHEME != dataUri?.scheme) {
                    coroutineScope.launch(Dispatchers.IO) {
                        val result = dataUri?.handleIntent(walletViewModel, coroutineScope)
                        if (result is LoaderResult.Single) {
                            withContext(Dispatchers.Main) {
                                navController.navigate("pass/${result.passId}")
                            }
                        }
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
                LaunchedEffect("Permissions") {
                    coroutineScope.launch(Dispatchers.IO) { permissionState.launchPermissionRequest() }
                }
            }
            val oledDark by settingsStore.oledDarkState.collectAsState()
            WalletTheme(oledDark = oledDark) {
                Box(
                    modifier =
                        androidx.compose.ui.Modifier
                            .fillMaxSize(),
                ) {
                    WalletApp(
                        navController,
                    )

                    if (isProcessingFileShare) {
                        Box(
                            modifier =
                                androidx.compose.ui.Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }

    private suspend fun Uri.handleIntent(
        walletViewModel: WalletViewModel,
        coroutineScope: CoroutineScope,
    ): LoaderResult {
        contentResolver.openInputStream(this).use {
            it?.let {
                return Loader(this@MainActivity).handleInputStream(
                    it,
                    walletViewModel,
                    coroutineScope,
                )
            }
        }

        return LoaderResult.Invalid
    }

    @Suppress("DEPRECATION")
    private fun Intent.sharedFileUri(): Uri? {
        if (action != Intent.ACTION_SEND) return null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)?.let { return it }
        } else {
            getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { return it }
        }

        return clipData?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.uri
    }
}
