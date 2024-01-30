package nz.eloque.foss_wallet.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TurnLeft
import androidx.compose.material.icons.filled.TurnRight
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.MainActivity
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.app.AppViewModelProvider
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.ui.components.PassView
import nz.eloque.foss_wallet.ui.wallet.PassViewModel
import nz.eloque.foss_wallet.ui.wallet.WalletView

sealed class Screen(val route: String, val icon: ImageVector, @StringRes val resourceId: Int) {
    data object Wallet : Screen("wallet", Icons.Filled.Wallet, R.string.wallet)
}

@Composable
fun WalletApp(
    activity: MainActivity,
    modifier: Modifier = Modifier
) {
    val passViewModel: PassViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = modifier
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Wallet.route,
        ) {
            composable(Screen.Wallet.route) {
                WalletScaffold(
                    navController = navController,
                    title = stringResource(id = R.string.wallet)
                ) {
                    WalletView(navController, passViewModel)
                }
            }
            composable(
                route = "pass/{passId}",
                arguments = listOf(navArgument("passId") { type = NavType.IntType })
            ) { backStackEntry ->
                val passId = backStackEntry.arguments?.getInt("passId")!!

                val pass = remember { mutableStateOf(Pass.placeholder())}
                LaunchedEffect(coroutineScope) {
                    coroutineScope.launch(Dispatchers.IO) {
                        pass.value = passViewModel.passById(passId)
                    }
                }

                val passViewFront = remember { mutableStateOf(true) }

                WalletScaffold(
                    navController = navController,
                    title = stringResource(R.string.pass),
                    toolWindow = true,
                    bottomBar = {
                        BottomAppBar {
                            NavigationBarItem(
                                selected = passViewFront.value,
                                onClick = { passViewFront.value = !passViewFront.value },
                                icon = {Icon(imageVector = Icons.Filled.TurnLeft, contentDescription = stringResource(R.string.front_side)) },
                                label = { Text(stringResource(R.string.front_side)) },
                            )
                            NavigationBarItem(
                                selected = !passViewFront.value,
                                onClick = { passViewFront.value = !passViewFront.value },
                                icon = { Icon(imageVector = Icons.Filled.TurnRight, contentDescription = stringResource(R.string.back_side)) },
                                label = { Text(stringResource(R.string.back_side)) },
                            )
                        }
                    }

                ) {
                    PassView(pass.value, passViewFront.value)
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
    bottomBar: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val items = listOf(
        Screen.Wallet,
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (toolWindow && showBack) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    }
                },
                actions = actions
            )
        },
        bottomBar = {
            if (!toolWindow) {
                BottomAppBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.route) },
                            label = { Text(stringResource(screen.resourceId)) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            } else {
                bottomBar.invoke()
            }
        }
    ) { innerPadding ->
        Box(modifier = modifier
            .padding(innerPadding)
            .padding(10.dp)) {
            content.invoke()
        }
    }
}
