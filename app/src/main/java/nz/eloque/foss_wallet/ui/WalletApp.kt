@file:OptIn(ExperimentalMaterial3Api::class)

package nz.eloque.foss_wallet.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import nz.eloque.compose_kit.navigation.slideBackward
import nz.eloque.compose_kit.navigation.slideForward
import nz.eloque.foss_wallet.shortcut.ShortcutService
import nz.eloque.foss_wallet.ui.screens.LibrariesScreen
import nz.eloque.foss_wallet.ui.screens.UpdateFailureScreen
import nz.eloque.foss_wallet.ui.screens.about.AboutScreen
import nz.eloque.foss_wallet.ui.screens.archive.ArchiveScreen
import nz.eloque.foss_wallet.ui.screens.create.AdvancedAddScreen
import nz.eloque.foss_wallet.ui.screens.create.CreateScreen
import nz.eloque.foss_wallet.ui.screens.pass.PassScreen
import nz.eloque.foss_wallet.ui.screens.pass.PassViewModel
import nz.eloque.foss_wallet.ui.screens.scan.ScanScreen
import nz.eloque.foss_wallet.ui.screens.scan.ScanViewModel
import nz.eloque.foss_wallet.ui.screens.settings.SettingsScreen
import nz.eloque.foss_wallet.ui.screens.settings.SettingsViewModel
import nz.eloque.foss_wallet.ui.screens.wallet.WalletScreen
import nz.eloque.foss_wallet.ui.screens.webview.WebviewScreen

@Composable
fun WalletApp(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    passViewModel: PassViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    scanViewModel: ScanViewModel = viewModel(),
) {
    Surface(
        modifier =
            modifier
                .fillMaxSize(),
    ) {
        NavHost(
            navController = navController,
            startDestination = Route.Wallet,
            enterTransition = { slideForward().targetContentEnter },
            exitTransition = { slideForward().initialContentExit },
            popEnterTransition = { slideBackward().targetContentEnter },
            popExitTransition = { slideBackward().initialContentExit },
        ) {
            composable<Route.Wallet> {
                WalletScreen(navController)
            }
            composable<Route.Scan> {
                ScanScreen(navController, scanViewModel)
            }
            composable<Route.Archive> {
                ArchiveScreen(navController)
            }
            composable<Route.About> {
                AboutScreen(navController)
            }
            composable<Route.Webview> { backStackEntry ->
                WebviewScreen(navController, backStackEntry.toRoute<Route.Webview>().url)
            }
            composable<Route.Settings> {
                SettingsScreen(navController, settingsViewModel)
            }
            composable<Route.Libraries> {
                LibrariesScreen(navController)
            }
            composable<Route.Create> {
                CreateScreen(navController)
            }
            composable<Route.AdvancedAdd> {
                AdvancedAddScreen(navController)
            }
            composable<Route.Pass>(
                deepLinks = listOf(navDeepLink<Route.Pass>(basePath = ShortcutService.BASE_URI)),
            ) { backStackEntry ->
                PassScreen(backStackEntry.toRoute<Route.Pass>().passId, navController, passViewModel)
            }
            composable<Route.UpdateFailure> { backStackEntry ->
                val route = backStackEntry.toRoute<Route.UpdateFailure>()
                UpdateFailureScreen(route.reason, route.rationale, navController)
            }
        }
    }
}
