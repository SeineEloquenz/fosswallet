@file:OptIn(ExperimentalMaterial3Api::class)

package nz.eloque.foss_wallet.ui

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.shortcut.Shortcut
import nz.eloque.foss_wallet.ui.screens.LibrariesScreen
import nz.eloque.foss_wallet.ui.screens.UpdateFailureScreen
import nz.eloque.foss_wallet.ui.screens.about.AboutScreen
import nz.eloque.foss_wallet.ui.screens.archive.ArchiveScreen
import nz.eloque.foss_wallet.ui.screens.pass.PassScreen
import nz.eloque.foss_wallet.ui.screens.settings.SettingsScreen
import nz.eloque.foss_wallet.ui.screens.settings.SettingsViewModel
import nz.eloque.foss_wallet.ui.screens.wallet.PassViewModel
import nz.eloque.foss_wallet.ui.screens.wallet.WalletScreen

sealed class Screen(val route: String, val icon: ImageVector, @param:StringRes val resourceId: Int) {
    data object Wallet : Screen("wallet", Icons.Default.Wallet, R.string.wallet)
    data object Archive : Screen("archive", Icons.Default.Archive, R.string.archive)
    data object About : Screen("about", Icons.Default.Info, R.string.about)
    data object Settings : Screen("settings", Icons.Default.Settings, R.string.settings)
    data object Libraries : Screen("libraries", Icons.AutoMirrored.Filled.LibraryBooks, R.string.libraries)
}

@Composable
fun WalletApp(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    passViewModel: PassViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Surface(
        modifier = modifier
            .fillMaxSize()
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Wallet.route,
            enterTransition = { slideIntoContainer(SlideDirection.Start, tween()) },
            exitTransition = { slideOutOfContainer(SlideDirection.Start, tween()) },
            popEnterTransition = { slideIntoContainer(SlideDirection.End, tween()) },
            popExitTransition = { slideOutOfContainer(SlideDirection.End, tween()) }
        ) {
            composable(Screen.Wallet.route) {
                WalletScreen(navController, passViewModel, snackbarHostState)
            }
            composable(Screen.Archive.route) {
                ArchiveScreen(navController, passViewModel)
            }
            composable(Screen.About.route) {
                AboutScreen(navController)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(navController, passViewModel, settingsViewModel)
            }
            composable(Screen.Libraries.route) {
                LibrariesScreen(navController)
            }
            composable(
                route = "pass/{passId}",
                deepLinks = listOf(navDeepLink {
                    uriPattern = "${Shortcut.BASE_URI}/{passId}"
                }),
                arguments = listOf(navArgument("passId") { type = NavType.StringType })
            ) { backStackEntry ->
                val passId = backStackEntry.arguments?.getString("passId")!!
                PassScreen(passId, navController, passViewModel)
            }
            composable(
                route = "updateFailure/{reason}/{rationale}",
                arguments = listOf(
                    navArgument("reason") { type = NavType.StringType },
                    navArgument("rationale") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val reason = backStackEntry.arguments?.getString("reason")!!
                val rationale = backStackEntry.arguments?.getString("rationale")!!
                UpdateFailureScreen(reason, rationale, navController)
            }
        }
    }
}
