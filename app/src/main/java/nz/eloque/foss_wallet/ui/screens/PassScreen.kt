package nz.eloque.foss_wallet.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.ui.WalletScaffold
import nz.eloque.foss_wallet.ui.view.pass.PassShareButton
import nz.eloque.foss_wallet.ui.view.pass.PassView
import nz.eloque.foss_wallet.ui.view.wallet.PassViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassScreen(
    passId: String,
    navController: NavHostController,
    passViewModel: PassViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val pass = remember { mutableStateOf(Pass.placeholder())}
    LaunchedEffect(coroutineScope) {
        coroutineScope.launch(Dispatchers.IO) {
            pass.value = passViewModel.passById(passId).applyLocalization(Locale.getDefault().language)
        }
    }

    WalletScaffold(
        navController = navController,
        title = pass.value.description,
        toolWindow = true,
        actions = {
            Row {
                if (pass.value.updatable()) {
                    val updateSuccessful = stringResource(R.string.update_successful)
                    val updateFailed = stringResource(R.string.update_failed)
                    IconButton(onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            val result = passViewModel.update(pass.value)
                            withContext(Dispatchers.Main) {
                                if (result != null) {
                                    pass.value = result
                                    Toast.makeText(context, updateSuccessful, Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, updateFailed, Toast.LENGTH_SHORT).show()
                                }
                            }
                            passViewModel.update(pass.value)?.let { pass.value = it}
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = stringResource(R.string.update))
                    }
                }
                val passFile = pass.value.originalPassFile(context)
                if (passFile != null) {
                    PassShareButton(passFile)
                }
                IconButton(onClick = {
                    coroutineScope.launch(Dispatchers.IO) { passViewModel.delete(pass.value) }
                    navController.popBackStack()
                }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                }
            }
        },
    ) { scrollBehavior ->
        PassView(pass.value, scrollBehavior = scrollBehavior)
    }
}