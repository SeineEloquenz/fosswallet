package nz.eloque.foss_wallet

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.fragment.app.FragmentActivity
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
import nz.eloque.foss_wallet.ui.WalletApp
import nz.eloque.foss_wallet.ui.screens.wallet.WalletViewModel
import nz.eloque.foss_wallet.ui.theme.WalletTheme

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val walletViewModel: WalletViewModel by viewModels()

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dataUri = when {
            Intent.ACTION_VIEW == intent.action -> intent.data
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
            LaunchedEffect(dataUri) {
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
                WalletApp(
                    navController,
                )
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
}
