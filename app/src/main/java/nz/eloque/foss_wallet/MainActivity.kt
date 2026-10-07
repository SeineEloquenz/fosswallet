package nz.eloque.foss_wallet

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nz.eloque.foss_wallet.persistence.loader.Loader
import nz.eloque.foss_wallet.persistence.loader.LoaderResult
import nz.eloque.foss_wallet.shortcut.ShortcutService
import nz.eloque.foss_wallet.ui.Screen
import nz.eloque.foss_wallet.ui.WalletApp
import nz.eloque.foss_wallet.ui.screens.create.ScanActivity
import nz.eloque.foss_wallet.ui.screens.create.scanSource
import nz.eloque.foss_wallet.ui.screens.scan.ScanLauncher
import nz.eloque.foss_wallet.ui.screens.wallet.WalletViewModel
import nz.eloque.foss_wallet.ui.theme.WalletTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val walletViewModel: WalletViewModel by viewModels()

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dataUri =
            when {
                Intent.ACTION_VIEW == intent.action -> {
                    intent.data
                }

                Intent.ACTION_SEND == intent.action -> {
                    intent.sharedFileUri()
                }

                else -> {
                    null
                }
            }

        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val coroutineScope = rememberCoroutineScope()
            val previewLauncher =
                ScanLauncher.launch(
                    onScanned = { Screen.Scan.navigate(navController, it) },
                    onCanceled = {},
                )
            var importHandled by rememberSaveable { mutableStateOf(false) }
            LaunchedEffect(dataUri) {
                if (importHandled) return@LaunchedEffect
                importHandled = true
                val source =
                    if (dataUri != null && ShortcutService.SCHEME != dataUri.scheme) {
                        withContext(Dispatchers.IO) { contentResolver.scanSource(dataUri, intent.type) }
                    } else {
                        null
                    }
                if (source != null) {
                    previewLauncher.launch(
                        Intent(this@MainActivity, ScanActivity::class.java).apply {
                            setDataAndType(dataUri, intent.type)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        },
                    )
                    return@LaunchedEffect
                }

                if (ShortcutService.SCHEME != dataUri?.scheme) {
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

        return clipData?.let { clip ->
            (0 until clip.itemCount).firstNotNullOfOrNull { clip.getItemAt(it).uri }
        }
    }
}
