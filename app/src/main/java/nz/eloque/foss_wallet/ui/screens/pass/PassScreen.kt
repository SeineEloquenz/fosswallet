package nz.eloque.foss_wallet.ui.screens.pass

import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppShortcut
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.api.FailureReason
import nz.eloque.foss_wallet.api.UpdateContent
import nz.eloque.foss_wallet.api.UpdateResult
import nz.eloque.foss_wallet.model.LocalizedPassWithTags
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.shortcut.Shortcut
import nz.eloque.foss_wallet.ui.AllowOnLockscreen
import nz.eloque.foss_wallet.ui.WalletScaffold
import nz.eloque.foss_wallet.utils.Biometric
import nz.eloque.foss_wallet.utils.asString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassScreen(
    passId: String,
    navController: NavHostController,
    passViewModel: PassViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val passFlow: Flow<LocalizedPassWithTags> = passViewModel.passFlowById(passId).mapNotNull { it ?: LocalizedPassWithTags.placeholder() }
    val localizedPass by remember(passFlow) { passFlow }.collectAsState(initial = LocalizedPassWithTags.placeholder())

    val tagFlow = passViewModel.allTags
    val allTags by remember(tagFlow) { tagFlow }.collectAsState(initial = setOf())
    
    val isAuthenticated by passViewModel.isAuthenticated.collectAsState()

    AllowOnLockscreen {
        val snackbarHostState = remember { SnackbarHostState() }
        WalletScaffold(
            snackbarHostState = snackbarHostState,
            navController = navController,
            title = localizedPass.pass.description,
            toolWindow = true,
            actions = {
                Actions(localizedPass.pass, navController, snackbarHostState, passViewModel, isAuthenticated = isAuthenticated)
            },
        ) { scrollBehavior ->
            PassView(
                localizedPass = localizedPass,
                allTags = allTags,
                onTagClick = { coroutineScope.launch(Dispatchers.IO) { passViewModel.untag(localizedPass.pass, it) } },
                onTagAdd = { coroutineScope.launch(Dispatchers.IO) { passViewModel.tag(localizedPass.pass, it) } },
                onTagCreate = { coroutineScope.launch(Dispatchers.IO) { passViewModel.addTag(it) } },
                barcodePosition = passViewModel.barcodePosition(),
                scrollBehavior = scrollBehavior,
                increaseBrightness = passViewModel.increasePassViewBrightness(),
                onRenderingChange = { coroutineScope.launch(Dispatchers.IO) { passViewModel.toggleLegacyRendering(localizedPass.pass) } },
            )
        }
    }
}

@Composable
fun Actions(
    pass: Pass,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    passViewModel: PassViewModel,
    isAuthenticated: Boolean,
) {
    val context = LocalContext.current
    val resources = LocalResources.current

    val activity = remember(context) { context as FragmentActivity }
    val coroutineScope = rememberCoroutineScope()

    val biometric = remember { Biometric(activity, snackbarHostState, coroutineScope) }

    val expanded = remember { mutableStateOf(false) }
    val isLoading = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .padding(16.dp)
    ) {
        IconButton(onClick = { expanded.value = !expanded.value }) {
            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more_options))
        }

        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            if (pass.pinned) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.unpin)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.PushPin,
                            contentDescription = stringResource(R.string.unpin)
                        )
                    },
                    onClick = { passViewModel.unpin(pass) }
                )
            } else {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.pin)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = stringResource(R.string.pin)
                        )
                    },
                    onClick = { passViewModel.pin(pass) }
                )
            }

            DropdownMenuItem(
                text = { Text(stringResource(R.string.add_shortcut)) },
                leadingIcon =  {
                    Icon(imageVector = Icons.Default.AppShortcut, contentDescription = stringResource(R.string.add_shortcut))
                },
                onClick = {
                    Shortcut.create(context, pass, pass.description)
                }
            )

            val passFile = pass.originalPassFile(context)
            if (passFile != null) {
                PassShareButton(passFile)
            }

            if (pass.updatable()) {
                val uriHandler = LocalUriHandler.current
                UpdateButton(isLoading = isLoading.value) {
                    coroutineScope.launch(Dispatchers.IO) {
                        isLoading.value = true
                        val result = passViewModel.update(pass)
                        isLoading.value = false
                        when (result) {
                            is UpdateResult.Success -> if (result.content is UpdateContent.Pass) {
                                snackbarHostState.showSnackbar(
                                    message = resources.getString(R.string.update_successful),
                                    duration = SnackbarDuration.Short
                                )
                            }
                            is UpdateResult.NotUpdated -> snackbarHostState.showSnackbar(message = resources.getString(R.string.status_not_updated))
                            is UpdateResult.Failed -> {
                                val snackResult = snackbarHostState.showSnackbar(
                                    message = when (result.reason) {
                                        is FailureReason.Status -> resources.getString(result.reason.messageId, result.reason.status)
                                        else -> resources.getString(result.reason.messageId)
                                    },
                                    actionLabel = if (result.reason is FailureReason.Detailed) resources.getString(R.string.details) else null,
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
                }
            }

            if (pass.hidden) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.unhide)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = stringResource(R.string.unhide)
                        )
                    },
                    onClick = {
                        if (isAuthenticated) {
                            passViewModel.unhide(pass)
                        } else {
                            biometric.prompt(
                                description = resources.getString(R.string.unhide),
                                onSuccess = { passViewModel.unhide(pass) }
                            )
                        }
                    }
                )
            } else {
                DropdownMenuItem(
                    text = { Text(resources.getString(R.string.hide)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = stringResource(R.string.hide)
                        )
                    },
                    onClick = {
                        if (isAuthenticated) {
                            passViewModel.hide(pass)
                        } else {
                            biometric.prompt(
                                description =  resources.getString(R.string.hide),
                                onSuccess = { passViewModel.hide(pass) }
                            )
                        }
                    }
                )
            }
            
            if (pass.archived) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.unarchive)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Unarchive,
                            contentDescription = stringResource(R.string.unarchive)
                        )
                    },
                    onClick = { passViewModel.unarchive(pass) }
                )
            } else {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.archive)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Archive,
                            contentDescription = stringResource(R.string.archive)
                        )
                    },
                    onClick = { passViewModel.archive(pass) }
                )
            }

            DropdownMenuItem(
                text = { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) },
                leadingIcon =  {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
                },
                onClick = {
                    coroutineScope.launch(Dispatchers.IO) { passViewModel.delete(pass) }
                    navController.popBackStack()
                    Toast.makeText(context, resources.getString(R.string.pass_deleted), Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun UpdateButton(
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "updateButtonAnimation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    DropdownMenuItem(
        text = { Text(stringResource(R.string.update)) },
        leadingIcon = {
            Icon(imageVector = Icons.Default.Sync, contentDescription = stringResource(R.string.update), modifier = Modifier.graphicsLayer(
                rotationZ = if (isLoading) rotation else 0f
            ))
        },
        onClick = { if (!isLoading) onClick() },
    )
}
