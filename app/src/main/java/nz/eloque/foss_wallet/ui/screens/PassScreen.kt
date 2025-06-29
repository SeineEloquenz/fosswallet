package nz.eloque.foss_wallet.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppShortcut
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.api.FailureReason
import nz.eloque.foss_wallet.api.UpdateContent
import nz.eloque.foss_wallet.api.UpdateResult
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.shortcut.Shortcut
import nz.eloque.foss_wallet.ui.AllowOnLockscreen
import nz.eloque.foss_wallet.ui.WalletScaffold
import nz.eloque.foss_wallet.ui.view.pass.PassShareButton
import nz.eloque.foss_wallet.ui.view.pass.PassView
import nz.eloque.foss_wallet.ui.view.wallet.PassViewModel
import nz.eloque.foss_wallet.utils.asString
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

    AllowOnLockscreen {
        val snackbarHostState = remember { SnackbarHostState() }
        WalletScaffold(
            snackbarHostState = snackbarHostState,
            navController = navController,
            title = pass.value.description,
            toolWindow = true,
            actions = {
                Row {
                    if (pass.value.updatable()) {
                        val uriHandler = LocalUriHandler.current
                        IconButton(onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                val result = passViewModel.update(pass.value)
                                when (result) {
                                    is UpdateResult.Success -> if (result.content is UpdateContent.Pass) {
                                        pass.value = result.content.pass
                                        snackbarHostState.showSnackbar(
                                            message = context.getString(R.string.update_successful),
                                            actionLabel = context.getString(R.string.details),
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    is UpdateResult.NotUpdated -> snackbarHostState.showSnackbar(message = context.getString(R.string.status_not_updated))
                                    is UpdateResult.Failed -> {
                                        val snackResult = snackbarHostState.showSnackbar(
                                            message = when (result.reason) {
                                                is FailureReason.Status -> context.getString(result.reason.messageId, result.reason.status)
                                                else -> context.getString(result.reason.messageId)
                                            },
                                            actionLabel = if (result.reason is FailureReason.Detailed) context.getString(R.string.details) else null,
                                            duration = SnackbarDuration.Short
                                        )
                                        if (snackResult == SnackbarResult.ActionPerformed && result.reason is FailureReason.Detailed) {
                                            when (result.reason) {
                                                is FailureReason.Exception -> coroutineScope.launch(Dispatchers.Main) { navController.navigate("updateFailure/${result.reason.exception.message}/${result.reason.exception.asString()}") }
                                                is FailureReason.Status -> uriHandler.openUri("https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Status/${result.reason.status}")
                                            }
                                        }
                                    }
                                }
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
                        Shortcut.create(context, pass.value, pass.value.description)
                    }) {
                        Icon(imageVector = Icons.Default.AppShortcut, contentDescription = stringResource(R.string.add_shortcut))
                    }
                }
            },
        ) { scrollBehavior ->
            PassView(pass.value, passViewModel.barcodePosition(), scrollBehavior = scrollBehavior)
        }
    }
}