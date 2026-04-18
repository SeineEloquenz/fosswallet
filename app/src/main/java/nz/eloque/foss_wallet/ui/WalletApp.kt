@file:OptIn(ExperimentalMaterial3Api::class)

package nz.eloque.foss_wallet.ui

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ContentPasteGo
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.zxing.BarcodeFormat
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.shortcut.Shortcut
import nz.eloque.foss_wallet.ui.screens.LibrariesScreen
import nz.eloque.foss_wallet.ui.screens.UpdateFailureScreen
import nz.eloque.foss_wallet.ui.screens.about.AboutScreen
import nz.eloque.foss_wallet.ui.screens.archive.ArchiveScreen
import nz.eloque.foss_wallet.ui.screens.create.AdvancedAddScreen
import nz.eloque.foss_wallet.ui.screens.create.CreateScreen
import nz.eloque.foss_wallet.ui.screens.create.CreateViewModel
import nz.eloque.foss_wallet.ui.screens.pass.PassScreen
import nz.eloque.foss_wallet.ui.screens.pass.PassViewModel
import nz.eloque.foss_wallet.ui.screens.scan.ScanScreen
import nz.eloque.foss_wallet.ui.screens.scan.ScanViewModel
import nz.eloque.foss_wallet.ui.screens.settings.SettingsScreen
import nz.eloque.foss_wallet.ui.screens.settings.SettingsViewModel
import nz.eloque.foss_wallet.ui.screens.wallet.WalletScreen
import nz.eloque.foss_wallet.ui.screens.wallet.WalletViewModel
import nz.eloque.foss_wallet.ui.screens.webview.WebviewScreen
import java.net.URLDecoder
import java.nio.charset.Charset

sealed class Screen(
    val route: String,
    val icon: ImageVector,
    @param:StringRes val resourceId: Int,
) {
    data object Scan : Screen("scan", Icons.Default.QrCodeScanner, R.string.barcode)

    data object Wallet : Screen("wallet", Icons.Default.Wallet, R.string.wallet)

    data object Archive : Screen("archive", Icons.Default.Archive, R.string.the_archive)

    data object About : Screen("about", Icons.Default.Info, R.string.about)

    data object Settings : Screen("settings", Icons.Default.Settings, R.string.settings)

    data object Libraries : Screen("libraries", Icons.AutoMirrored.Filled.LibraryBooks, R.string.libraries)

    data object Create : Screen("create", Icons.Default.Create, R.string.create_pass) {
        const val BARCODE_ROUTE = "create?format={format}?message={message}?altText={altText}?encoding={encoding}"

        val NAV_ARGUMENTS =
            listOf(
                navArgument("message") { type = NavType.StringType },
                navArgument("altText") { type = NavType.StringType },
                navArgument("encoding") { type = NavType.StringType },
                navArgument("format") { type = NavType.StringType },
            )

        fun navigate(
            navController: NavHostController,
            barCode: BarCode,
        ) {
            navController.navigate(
                "create?format=${barCode.format}?message=${barCode.message}?altText=${barCode.altText}?encoding=${barCode.encoding}",
            )
        }
    }

    data object AdvancedAdd : Screen("advanced_add", Icons.Default.MoreHoriz, R.string.advanced)

    data object Web : Screen("webview", Icons.Default.ContentPasteGo, R.string.webview)
}

@Composable
fun WalletApp(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    createViewModel: CreateViewModel = viewModel(),
    passViewModel: PassViewModel = viewModel(),
    walletViewModel: WalletViewModel = viewModel(),
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
            startDestination = Screen.Wallet.route,
            enterTransition = { slideIntoContainer(SlideDirection.Start, tween()) },
            exitTransition = { slideOutOfContainer(SlideDirection.Start, tween()) },
            popEnterTransition = { slideIntoContainer(SlideDirection.End, tween()) },
            popExitTransition = { slideOutOfContainer(SlideDirection.End, tween()) },
        ) {
            composable(Screen.Wallet.route) {
                WalletScreen(navController, walletViewModel)
            }
            composable(Screen.Scan.route) {
                ScanScreen(navController, scanViewModel)
            }
            composable(Screen.Archive.route) {
                ArchiveScreen(navController, walletViewModel)
            }
            composable(Screen.About.route) {
                AboutScreen(navController)
            }
            composable(
                route = "webview/{url}",
                arguments = listOf(navArgument("url") { type = NavType.StringType }),
            ) { backStackEntry ->
                val rawUrl = backStackEntry.arguments?.getString("url")!!
                val url = URLDecoder.decode(rawUrl, Charsets.UTF_8.name())
                WebviewScreen(navController, walletViewModel, url)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(navController, settingsViewModel)
            }
            composable(Screen.Libraries.route) {
                LibrariesScreen(navController)
            }
            composable(Screen.Create.route) {
                CreateScreen(
                    navController = navController,
                    createViewModel = createViewModel,
                )
            }
            composable(
                route = Screen.Create.BARCODE_ROUTE,
                arguments = Screen.Create.NAV_ARGUMENTS,
            ) { backStackEntry ->
                val barcode =
                    BarCode(
                        format = BarcodeFormat.valueOf(backStackEntry.arguments?.getString("format")!!),
                        message = backStackEntry.arguments?.getString("message")!!,
                        encoding = Charset.forName(backStackEntry.arguments?.getString("encoding")!!),
                        altText = backStackEntry.arguments?.getString("altText"),
                    )

                CreateScreen(
                    navController,
                    createViewModel,
                    initialBarcode = barcode,
                )
            }
            composable(Screen.AdvancedAdd.route) {
                AdvancedAddScreen(navController)
            }
            composable(
                route = "pass/{passId}",
                deepLinks =
                    listOf(
                        navDeepLink {
                            uriPattern = "${Shortcut.BASE_URI}/{passId}"
                        },
                    ),
                arguments = listOf(navArgument("passId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val passId = backStackEntry.arguments?.getString("passId")!!
                PassScreen(passId, navController, passViewModel)
            }
            composable(
                route = "updateFailure/{reason}/{rationale}",
                arguments =
                    listOf(
                        navArgument("reason") { type = NavType.StringType },
                        navArgument("rationale") { type = NavType.StringType },
                    ),
            ) { backStackEntry ->
                val reason = backStackEntry.arguments?.getString("reason")!!
                val rationale = backStackEntry.arguments?.getString("rationale")!!
                UpdateFailureScreen(reason, rationale, navController)
            }
        }
    }
}
