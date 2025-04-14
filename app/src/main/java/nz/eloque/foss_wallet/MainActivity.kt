package nz.eloque.foss_wallet

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.app.AppViewModelProvider
import nz.eloque.foss_wallet.parsing.PassParser
import nz.eloque.foss_wallet.persistence.InvalidPassException
import nz.eloque.foss_wallet.persistence.PassLoader
import nz.eloque.foss_wallet.ui.WalletApp
import nz.eloque.foss_wallet.ui.theme.WalletTheme
import nz.eloque.foss_wallet.ui.wallet.PassViewModel


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dataUri = if (intent != null && intent.action == Intent.ACTION_VIEW) {
            intent.data
        } else {
            null
        }

        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val coroutineScope = rememberCoroutineScope()
            val passViewModel: PassViewModel = viewModel(factory = AppViewModelProvider.Factory)
            LaunchedEffect(dataUri) {
                coroutineScope.launch(Dispatchers.IO) {
                    dataUri?.handleIntent(passViewModel, coroutineScope, navController)
                }
            }
            WalletTheme {
                WalletApp(
                    this,
                    navController,
                )
            }
        }
    }

    private suspend fun Uri.handleIntent(passViewModel: PassViewModel, coroutineScope: CoroutineScope, navController: NavHostController) {
        contentResolver.openInputStream(this).use {
            it?.let {
                try {
                    val (pass, bitmaps, localizations) = PassLoader(PassParser(this@MainActivity)).load(it)
                    val id = passViewModel.add(pass, bitmaps, localizations)
                    coroutineScope.launch(Dispatchers.Main) { navController
                        .navigate("pass/$id") }
                } catch (e: InvalidPassException) {
                    Log.w(TAG, "Failed to load pass from intent: $e")
                    coroutineScope.launch(Dispatchers.Main) { Toast
                        .makeText(this@MainActivity, this@MainActivity.getString(R.string.invalid_pass_toast), Toast.LENGTH_SHORT)
                        .show() }
                }
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
