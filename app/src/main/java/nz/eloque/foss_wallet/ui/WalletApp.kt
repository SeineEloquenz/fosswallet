@file:OptIn(ExperimentalMaterial3Api::class)

package nz.eloque.foss_wallet.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderDelete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.persistence.InvalidPassException
import nz.eloque.foss_wallet.ui.about.AboutView
import nz.eloque.foss_wallet.ui.components.AbbreviatingText
import nz.eloque.foss_wallet.ui.components.pass_view.PassShareButton
import nz.eloque.foss_wallet.ui.components.pass_view.PassView
import nz.eloque.foss_wallet.ui.components.pass_view.PassViewBottomBar
import nz.eloque.foss_wallet.ui.wallet.PassViewModel
import nz.eloque.foss_wallet.ui.wallet.WalletView
import nz.eloque.foss_wallet.utils.isScrollingUp
import java.util.Locale

sealed class Screen(val route: String, val icon: ImageVector, @StringRes val resourceId: Int) {
    data object Wallet : Screen("wallet", Icons.Default.Wallet, R.string.wallet)
    data object About : Screen("about", Icons.Default.Info, R.string.about)
}

@Composable
fun WalletApp(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    passViewModel: PassViewModel = viewModel(),
) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = modifier
            .fillMaxSize()
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Wallet.route,
        ) {
            composable(Screen.Wallet.route) {
                val listState = rememberLazyListState()
                val toastMessage = stringResource(R.string.invalid_pass_toast)
                val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { res ->
                    res?.let {
                        println("selected file URI $res")
                        coroutineScope.launch(Dispatchers.IO) {
                            contentResolver.openInputStream(res)?.use { inputStream ->
                                try {
                                    passViewModel.load(context, inputStream)
                                } catch (_: InvalidPassException) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                }
                val passesToGroup = remember { mutableStateSetOf<Pass>() }

                WalletScaffold(
                    navController = navController,
                    title = stringResource(id = R.string.wallet),
                    actions = {
                        if (passesToGroup.size >= 2) {
                            IconButton(onClick = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    passViewModel.group(passesToGroup.toSet())
                                    passesToGroup.clear()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Merge,
                                    contentDescription = stringResource(R.string.group)
                                )
                            }
                        }
                        IconButton(onClick = {
                            navController.navigate(Screen.About.route)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = stringResource(R.string.about)
                            )
                        }
                    },
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            text = { Text(stringResource(R.string.add_pass)) },
                            icon = { Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.add_pass)) },
                            expanded = listState.isScrollingUp(),
                            onClick = { launcher.launch(arrayOf("*/*")) }
                        )
                    },
                ) { scrollBehavior ->
                    WalletView(navController, passViewModel, listState = listState, scrollBehavior = scrollBehavior, passesToGroup = passesToGroup)
                }
            }
            composable(Screen.About.route) {
                WalletScaffold(
                    navController = navController,
                    toolWindow = true,
                    title = stringResource(id = R.string.about)
                ) {
                    AboutView()
                }
            }
            composable(
                route = "pass/{passId}",
                arguments = listOf(navArgument("passId") { type = NavType.StringType })
            ) { backStackEntry ->
                val passId = backStackEntry.arguments?.getString("passId")!!

                val pass = remember { mutableStateOf(Pass.placeholder())}
                LaunchedEffect(coroutineScope) {
                    coroutineScope.launch(Dispatchers.IO) {
                        pass.value = passViewModel.passById(passId).applyLocalization(Locale.getDefault().language)
                    }
                }

                val passViewFront = remember { mutableStateOf(true) }
                WalletScaffold(
                    navController = navController,
                    title = pass.value.description,
                    toolWindow = true,
                    bottomBar = {
                        if (pass.value.backFields.isNotEmpty()) {
                            PassViewBottomBar(passViewFront)
                        }
                    },
                    actions = {
                        Row {
                            val groupId = pass.value.groupId
                            if (groupId != null) {
                                IconButton(onClick = {
                                    coroutineScope.launch(Dispatchers.IO) { passViewModel.deleteGroup(groupId) }
                                }) {
                                    Icon(imageVector = Icons.Default.FolderDelete, contentDescription = stringResource(R.string.ungroup))
                                }
                            }
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
                    PassView(pass.value, passViewFront.value, scrollBehavior = scrollBehavior)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScaffold(
    navController: NavController,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.wallet),
    toolWindow: Boolean = false,
    showBack: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (scrollBehavior: TopAppBarScrollBehavior) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { AbbreviatingText(
                    title,
                    maxLines = 1,
                ) },
                navigationIcon = {
                    if (toolWindow && showBack) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    }
                },
                actions = actions,
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton
    ) { innerPadding ->
        Box(modifier = modifier
            .padding(innerPadding)
            .padding(horizontal = 10.dp)
        ) {
            content.invoke(scrollBehavior)
        }
    }
}
