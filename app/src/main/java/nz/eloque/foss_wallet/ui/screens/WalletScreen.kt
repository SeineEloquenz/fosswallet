package nz.eloque.foss_wallet.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.api.ImportResult
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.persistence.InvalidPassException
import nz.eloque.foss_wallet.ui.Screen
import nz.eloque.foss_wallet.ui.WalletScaffold
import nz.eloque.foss_wallet.ui.view.wallet.PassViewModel
import nz.eloque.foss_wallet.ui.view.wallet.WalletView
import nz.eloque.foss_wallet.utils.isScrollingUp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    navController: NavHostController,
    passViewModel: PassViewModel,
) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val coroutineScope = rememberCoroutineScope()

    val listState = rememberLazyListState()
    val toastMessage = stringResource(R.string.invalid_pass_toast)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { res ->
        res?.let {
            println("selected file URI $res")
            coroutineScope.launch(Dispatchers.IO) {
                contentResolver.openInputStream(res)?.use { inputStream ->
                    try {
                        val importResult = passViewModel.load(context, inputStream)
                        withContext(Dispatchers.Main) {
                            when (importResult) {
                                is ImportResult.New -> {
                                    // Pass imported successfully
                                }
                                is ImportResult.Replaced -> {
                                    Toast.makeText(context, context.getString(R.string.pass_already_imported), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } catch (_: InvalidPassException) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
    val selectedPasses = remember { mutableStateSetOf<Pass>() }

    WalletScaffold(
        navController = navController,
        title = stringResource(id = R.string.wallet),
        actions = {
            IconButton(onClick = {
                navController.navigate(Screen.Settings.route)
            }) {
                Icon(
                    imageVector = Screen.Settings.icon,
                    contentDescription = stringResource(R.string.about)
                )
            }
        },
        floatingActionButton = {
            if (selectedPasses.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    FloatingActionButton(
                        containerColor = MaterialTheme.colorScheme.error,
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                selectedPasses.forEach { passViewModel.delete(it) }
                                selectedPasses.clear()
                            }
                        },
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                    }
                    ExtendedFloatingActionButton(
                        text = { Text(stringResource(R.string.group)) },
                        icon = { Icon(imageVector = Icons.Default.Folder, contentDescription = stringResource(R.string.group)) },
                        expanded = listState.isScrollingUp(),
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                passViewModel.group(selectedPasses.toSet())
                                selectedPasses.clear()
                            }
                        },
                    )
                }
            } else {
                ExtendedFloatingActionButton(
                    text = { Text(stringResource(R.string.add_pass)) },
                    icon = { Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.add_pass)) },
                    expanded = listState.isScrollingUp(),
                    onClick = { launcher.launch(arrayOf("*/*")) }
                )
            }
        },
    ) { scrollBehavior ->
        WalletView(navController, passViewModel, listState = listState, scrollBehavior = scrollBehavior, selectedPasses = selectedPasses)
    }
}