package nz.eloque.foss_wallet.ui.screens.wallet

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.LocalizedPassWithTags
import nz.eloque.foss_wallet.persistence.loader.Loader
import nz.eloque.foss_wallet.persistence.loader.LoaderResult
import nz.eloque.foss_wallet.ui.Screen
import nz.eloque.foss_wallet.ui.WalletScaffold
import nz.eloque.foss_wallet.utils.Biometric
import nz.eloque.foss_wallet.ui.components.FabMenu
import nz.eloque.foss_wallet.ui.components.FabMenuItem
import nz.eloque.foss_wallet.utils.PkpassMimeTypes
import java.net.URLEncoder

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    navController: NavHostController,
    passViewModel: PassViewModel
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val contentResolver = context.contentResolver
    val coroutineScope = rememberCoroutineScope()

    val listState = rememberLazyListState()
    val activity = remember(context) { context as FragmentActivity }
    val snackbarHostState = remember { SnackbarHostState() }
    val biometric = remember { Biometric(activity, snackbarHostState, coroutineScope) }

    val loading = remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        println("selected file URI $uris")
        coroutineScope.launch {
            loading.value = true
            withContext(Dispatchers.IO) {
                var result: LoaderResult? = null
                uris.forEach { uri ->
                    contentResolver.openInputStream(uri)?.use {
                        result = Loader(context).handleInputStream(
                            it,
                            passViewModel,
                            coroutineScope
                        )
                    }
                }
                if (uris.size == 1) {
                    if (result is LoaderResult.Single) {
                        withContext(Dispatchers.Main) {
                            navController.navigate("pass/${result.passId}")
                        }
                    }
                }
            }
            loading.value = false
        }
    }
    val selectedPasses = remember { mutableStateSetOf<LocalizedPassWithTags>() }

    WalletScaffold(
        navController = navController,
        title = stringResource(id = Screen.Wallet.resourceId),
        actions = {
            if (passViewModel.isAuthenticated) {
                IconButton(onClick = { passViewModel.conceal() }) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = stringResource(R.string.conceal)
                    )
                }
            } else {
                IconButton(
                    onClick = {
                        biometric.prompt(
                            description = context.getString(R.string.reveal),
                            onSuccess = { passViewModel.reveal() }
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = stringResource(R.string.reveal)
                    )
                }
            }

            IconButton(onClick = {
                navController.navigate(Screen.Archive.route)
            }) {
                Icon(
                    imageVector = Screen.Archive.icon,
                    contentDescription = stringResource(R.string.archive)
                )
            }

            IconButton(onClick = {
                navController.navigate(Screen.Settings.route)
            }) {
                Icon(
                    imageVector = Screen.Settings.icon,
                    contentDescription = stringResource(Screen.Settings.resourceId)
                )
            }
        },
        floatingActionButton = {
            if (selectedPasses.isNotEmpty()) {
                SelectionActions(
                    false,
                    selectedPasses,
                    listState,
                    passViewModel
                )
            } else {
                FabMenu(
                    items = listOf(
                        FabMenuItem(
                            icon = Screen.Web.icon,
                            title = stringResource(R.string.webview),
                            onClick = {
                                coroutineScope.launch {
                                    val entry = clipboard.getClipEntry()

                                    if (entry == null) {
                                        Toast.makeText(context, context.getString(R.string.no_url_in_clipboard), Toast.LENGTH_LONG).show()
                                        return@launch
                                    }

                                    for (i in 0 until entry.clipData.itemCount) {
                                        val item = entry.clipData.getItemAt(i)
                                        val string = item?.text.toString()
                                        if (string.startsWith("https://") || string.startsWith("http://")) {
                                            withContext(Dispatchers.Main) {
                                                navController.navigate("${Screen.Web.route}/${URLEncoder.encode(string, Charsets.UTF_8.name())}")
                                            }
                                            return@launch
                                        }
                                    }

                                    Toast.makeText(context, context.getString(R.string.no_url_in_clipboard), Toast.LENGTH_LONG).show()
                                }
                            }
                        ),
                        FabMenuItem(
                            icon = Icons.Default.Create,
                            title = stringResource(R.string.create_pass),
                            onClick = {
                                navController.navigate(Screen.Create.route)
                            }
                        ),
                        FabMenuItem(
                            icon = Icons.Default.Add,
                            title = stringResource(R.string.add_pass),
                            onClick = {
                                launcher.launch(arrayOf(
                                    "application/json+zip",
                                    "application/octet-stream",
                                    "text/json"
                                ).plus(PkpassMimeTypes))
                            }
                        )
                    )
                )
            }
        },
    ) { scrollBehavior ->
        WalletView(navController, passViewModel, listState = listState, scrollBehavior = scrollBehavior, selectedPasses = selectedPasses)

        if (loading.value) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
