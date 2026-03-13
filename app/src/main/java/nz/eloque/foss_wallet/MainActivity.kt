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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.fragment.app.FragmentActivity
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import nz.eloque.foss_wallet.shortcut.Shortcut
import nz.eloque.foss_wallet.ui.Screen
import nz.eloque.foss_wallet.ui.WalletApp
import nz.eloque.foss_wallet.ui.screens.create.ImageScanner
import nz.eloque.foss_wallet.ui.screens.wallet.WalletViewModel
import nz.eloque.foss_wallet.ui.theme.WalletTheme
import java.net.URLEncoder

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val walletViewModel: WalletViewModel by viewModels()

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isImageShare = intent.action == Intent.ACTION_SEND && intent.type?.startsWith("image/") == true
        val dataUri = when {
            Intent.ACTION_VIEW == intent.action -> intent.data
            isImageShare -> intent.sharedImageUri()
            Intent.ACTION_SEND == intent.action -> {
                val count = intent.clipData?.itemCount?.minus(1)?.coerceAtLeast(0)
                count?.let { intent.clipData?.getItemAt(it)?.uri }
            }
            else -> null
        }

        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val coroutineScope = rememberCoroutineScope()
            var isProcessingImageShare by remember { mutableStateOf(false) }
            LaunchedEffect(dataUri, isImageShare) {
                if (isImageShare && dataUri != null) {
                    isProcessingImageShare = true
                    coroutineScope.launch(Dispatchers.IO) {
                        val scanResult = runCatching { ImageScanner.scanFrom(contentResolver, dataUri) }.getOrNull()
                        withContext(Dispatchers.Main) {
                            isProcessingImageShare = false
                            val barcode = scanResult?.text
                            if (!barcode.isNullOrEmpty()) {
                                navController.navigate("${Screen.Create.route}?barcode=${URLEncoder.encode(barcode, Charsets.UTF_8.name())}")
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
            WalletTheme {
                Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                    WalletApp(
                        navController,
                    )

                    if (isProcessingImageShare) {
                        Box(
                            modifier = androidx.compose.ui.Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }

    private fun Uri.handleIntent(walletViewModel: WalletViewModel, coroutineScope: CoroutineScope): LoaderResult {
        contentResolver.openInputStream(this).use {
            it?.let {
                return Loader(this@MainActivity).handleInputStream(
                    it,
                    walletViewModel,
                    coroutineScope
                )
            }
        }

        return LoaderResult.Invalid
    }

    @Suppress("DEPRECATION")
    private fun Intent.sharedImageUri(): Uri? {
        if (action != Intent.ACTION_SEND) return null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)?.let { return it }
        } else {
            getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { return it }
        }

        return clipData?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.uri
    }
}
